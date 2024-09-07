package ru.itmo.hub.kafka;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.itmo.shared.ActionResult;
import ru.itmo.shared.Pair;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class Conversator {
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ConcurrentMap<Long, Pair<CountDownLatch, ActionResult>> consumerData = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @KafkaListener(topics = {"request-response"}, containerFactory = "actionResultKafkaListenerContainerFactory")
    private void responseListener(@Header(KafkaHeaders.RECEIVED_KEY) Long key,
                                  @Payload ActionResult result) {
        var pair = consumerData.get(key);
        if (pair != null) {
            pair.setSecond(result);
            pair.getFirst().countDown();
        }
    }

    @SneakyThrows
    public Optional<String> dialogueSync(@NonNull TopicConversationData conversationData) {
        var uniqueId = random.nextLong();
        var cdLatch = new CountDownLatch(1);
        consumerData.put(uniqueId, new Pair<>(cdLatch, null));
        var headers = List.<org.apache.kafka.common.header.Header>of(new RecordHeader("returnTopic", "request-response".getBytes()));
        var data = conversationData.getSerialized();
        send(new ProducerRecord<>(conversationData.getTopic(), null, null, uniqueId, data, headers));

        var isOnTime = cdLatch.await(30, TimeUnit.SECONDS);
        if (isOnTime) {
            var response = consumerData.get(uniqueId).getSecond();
            consumerData.remove(uniqueId);
            return response.toOptional();
        } else {
            log.error("Didn't get response after sending to topic " + conversationData.getTopic());
            consumerData.remove(uniqueId);
            return Optional.empty();
        }
    }

    @SneakyThrows
    public void dialogueAsync(@NonNull Long tid, @NonNull TopicConversationData conversationData) {
        var data = conversationData.getSerialized();
        var headers = List.<org.apache.kafka.common.header.Header>of(new RecordHeader("returnTopic", "response".getBytes()));
        send(new ProducerRecord<>(conversationData.getTopic(), null, null, tid, data, headers));
    }

    @SneakyThrows
    public void monologue(@NonNull TopicConversationData conversationData) {
        var data = conversationData.getSerialized();
        send(new ProducerRecord<>(conversationData.getTopic(), 0L, data));
    }

    private void send(ProducerRecord<Long, String> producerRecord) {
        kafkaTemplate.send(producerRecord);
        String returnTopicMessage = "";
        var returnTopic = producerRecord.headers().lastHeader("returnTopic");
        if (returnTopic != null) {
            returnTopicMessage = " Return topic is " + new String(returnTopic.value()) + ".";
        }
        log.info("Sent message to topic " + producerRecord.topic() + " with key=" + producerRecord.key()
                + " and body=" + producerRecord.value() + "." + returnTopicMessage);
    }
}
