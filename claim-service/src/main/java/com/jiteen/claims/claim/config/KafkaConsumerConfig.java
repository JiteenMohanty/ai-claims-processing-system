package com.jiteen.claims.claim.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jiteen.claims.claim.domain.event.AiAnalysisCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Kafka consumer infrastructure configuration for the Claim Service.
 *
 * <p>
 * This configuration class defines the consumer factory and listener container
 * factory beans used by the {@link com.jiteen.claims.claim.application.event.ClaimEventConsumer}
 * to consume {@link AiAnalysisCompletedEvent} messages from the
 * {@value KafkaTopics#AI_ANALYSIS_COMPLETED} topic.
 * </p>
 *
 * <p>
 * A custom {@link ObjectMapper} with {@link JavaTimeModule} registration is
 * injected into the {@link JsonDeserializer} to ensure consistent
 * {@code LocalDateTime} deserialization from ISO-8601 string format across
 * service boundaries without relying on Kafka type header coupling.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Constructs a typed {@link ConsumerFactory} configured to deserialize
     * {@link AiAnalysisCompletedEvent} payloads from JSON message bodies.
     *
     * @return a fully configured {@link ConsumerFactory} for AI analysis events
     */
    @Bean
    public ConsumerFactory<String, AiAnalysisCompletedEvent> consumerFactory() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonDeserializer<AiAnalysisCompletedEvent> deserializer =
                new JsonDeserializer<>(AiAnalysisCompletedEvent.class, objectMapper);
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    /**
     * Constructs the {@link ConcurrentKafkaListenerContainerFactory} bean that
     * powers all {@code @KafkaListener} consumer methods within the Claim Service.
     *
     * @return a configured {@link ConcurrentKafkaListenerContainerFactory} instance
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AiAnalysisCompletedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AiAnalysisCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
