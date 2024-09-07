package ru.itmo.shared;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class HubExternalTaskHandler implements ExternalTaskHandler {
    private final ExternalTaskHandlerExtended externalTaskHandlerExtended;
    private final Optional<TransactionTemplate> transactionTemplate;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        var helper = new Helper(externalTask, externalTaskService);
        try {
            transactionTemplate.ifPresentOrElse(
                    t -> t.executeWithoutResult(status ->
                            externalTaskHandlerExtended.execute(externalTask, externalTaskService, helper)),
                    () -> externalTaskHandlerExtended.execute(externalTask, externalTaskService, helper));
        } catch (RuntimeException exc) {
            externalTaskService.handleBpmnError(externalTask, "java-exception", exc.getMessage());
        }
    }

    public interface ExternalTaskHandlerExtended {
        void execute(ExternalTask externalTask, ExternalTaskService externalTaskService, Helper helper);
    }

    @RequiredArgsConstructor
    public static class Helper {
        private final ExternalTask externalTask;
        private final ExternalTaskService externalTaskService;

        public <T> T getVarOrThrow(String key) {
            var value = externalTask.getAllVariables().get(key);
            if (value == null) {
                throw new RuntimeException("Variable '" + key + "' not found on task " + externalTask.getTopicName());
            }
            return (T) value;
        }

        public void completeWithVar(String varName, Object value) {
            externalTaskService.complete(externalTask, Map.of(varName, value));
        }
    }
}
