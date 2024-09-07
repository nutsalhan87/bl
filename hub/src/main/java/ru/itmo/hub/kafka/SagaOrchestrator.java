package ru.itmo.hub.kafka;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.shared.ActionResult;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.BiFunction;
import java.util.function.LongConsumer;

import static ru.itmo.shared.ActionResult.Fail;
import static ru.itmo.shared.ActionResult.Ok;

@Slf4j
@Service
public class SagaOrchestrator {
    private static final Random rnd = new Random();
    private final Map<Long, ActionData> actions = new ConcurrentHashMap<>();
    private final TaskExecutor taskExecutor;
    private final TaskScheduler taskScheduler;
    private final Conversator conversator;
    private final TransactionTemplate transactionTemplate;
    private final Integer secondTimeout = 30;

    public SagaOrchestrator(@Qualifier("taskExecutor") TaskExecutor taskExecutor,
                            @Qualifier("taskScheduler") TaskScheduler taskScheduler,
                            Conversator conversator,
                            TransactionTemplate transactionTemplate) {
        this.taskExecutor = taskExecutor;
        this.taskScheduler = taskScheduler;
        this.conversator = conversator;
        this.transactionTemplate = transactionTemplate;
    }

    @SneakyThrows
    @KafkaListener(topics = "response", containerFactory = "actionResultKafkaListenerContainerFactory")
    private void orchestrate(@Payload ActionResult result,
                             @Header(KafkaHeaders.RECEIVED_KEY) Long transactionId) {
        var actionData = actions.get(transactionId);
        if (actionData == null) {
            return;
        }
        actionData.semaphore.acquire();
        if (result instanceof Ok ok) {
            actionData.actionsIterator.previous().setPerformedSuccessfully();
            actionData.actionsIterator.next();
            if (actionData.actionsIterator.hasNext()) {
                taskExecutor.execute(task(transactionId, ok.value()));
            } else {
                log.info("Transaction " + transactionId + " completed.");
                actions.remove(transactionId);
            }
        } else if (result instanceof Fail fail) {
            log.error(fail.message());
            actions.remove(transactionId);
            actionData.compensationActions.remove(); // пропускаем последнее действие, т.к. оно неудачное и компенсирующая транзакция не нужна
            actionData.compensationActions.forEach(action -> {
                transactionTemplate.execute(status -> {
                    action.accept(transactionId);
                    return null;
                });
            });
        }
    }

    @SneakyThrows
    public void perform(@NonNull Action[] sagaActions, @NonNull String initialBody) {
        if (sagaActions.length == 0) {
            return;
        }
        var transactionId = rnd.nextLong();
        var actionData = new ActionData(sagaActions);
        actions.put(transactionId, actionData);
        taskExecutor.execute(task(transactionId, initialBody));
    }

    private Runnable task(@NonNull Long transactionId, @NonNull String body) {
        var actionData = actions.get(transactionId);
        return () -> {
            LongConsumer compensateAction;
            try {
                var performer = actionData.actionsIterator.next().performer;
                compensateAction = transactionTemplate.execute(status -> performer.apply(transactionId, body));
            } catch (Throwable t) {
                log.error(t.getMessage());
                compensateAction = tid -> {};
                conversator.dialogueAsync(transactionId, TopicConversationData.sagaResponse(new Fail(t.getMessage())));
            }
            actionData.compensationActions.add(compensateAction);
            actionData.semaphore.release();
            taskScheduler.schedule(() -> cancelOrchestrationIfTimeout(transactionId),
                    Instant.now().plusSeconds(secondTimeout));
        };
    }

    private void cancelOrchestrationIfTimeout(@NonNull Long transactionId) {
        var actionData = actions.get(transactionId);
        if (actionData != null && !actionData.actionsIterator.previous().performedSuccessfully) {
            var failMessage = "Transaction \" + transactionId + \" timed out.";
            conversator.dialogueAsync(transactionId, TopicConversationData.sagaResponse(new Fail(failMessage)));
        }
    }

    @RequiredArgsConstructor
    public static class Action {
        private final BiFunction<Long, String, LongConsumer> performer;
        private boolean performedSuccessfully = false;

        public void setPerformedSuccessfully() {
            this.performedSuccessfully = true;
        }
    }

    private static class ActionData {
        public ListIterator<Action> actionsIterator;
        public Queue<LongConsumer> compensationActions = new ArrayDeque<>();
        public Semaphore semaphore = new Semaphore(1);

        public ActionData(Action[] actionsIterator) throws InterruptedException {
            this.actionsIterator = Arrays.stream(actionsIterator).toList().listIterator();
            semaphore.acquire();
        }
    }
}
