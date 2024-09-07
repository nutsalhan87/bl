package ru.itmo.hub.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.itmo.shared.ActionResult;

import java.util.HashMap;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean("actionResultConsumerFactory")
    public ConsumerFactory<Long, ActionResult> actionResultConsumerFactory() {
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return new DefaultKafkaConsumerFactory<>(config,
                new LongDeserializer(),
                new JsonDeserializer<>(ActionResult.class));
    }

    @Bean
    @DependsOn("actionResultConsumerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, ActionResult>
    actionResultKafkaListenerContainerFactory(ConsumerFactory<Long, ActionResult> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, ActionResult>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean("actionResultProducerFactory")
    public ProducerFactory<Long, ActionResult> actionResultProducerFactory() {
        var config = new HashMap<String, Object>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new DefaultKafkaProducerFactory<>(config,
                new LongSerializer(),
                new JsonSerializer<>());
    }

    @Bean
    @DependsOn("actionResultProducerFactory")
    public KafkaTemplate<Long, ActionResult> actionResultKafkaTemplate(ProducerFactory<Long, ActionResult> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean("stringProducerFactory")
    public ProducerFactory<Long, String> stringProducerFactory() {
        var config = new HashMap<String, Object>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new DefaultKafkaProducerFactory<>(config,
                new LongSerializer(),
                new StringSerializer());
    }

    @Bean
    @DependsOn("stringProducerFactory")
    public KafkaTemplate<Long, String> stringKafkaTemplate(ProducerFactory<Long, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
