package com.jiteen.claims.ai.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.ai.domain.event.DocumentUploadedEvent;
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
 * Spring Kafka consumer infrastructure configuration for the AI Processing Service.
 *
 * <p>
 * Provides two independent typed consumer factories:
 * <ul>
 *   <li>{@code kafkaListenerContainerFactory} — for {@link ClaimCreatedEvent} payloads
 *       from the {@value KafkaTopics#CLAIM_CREATED} topic.</li>
 *   <li>{@code documentUploadedListenerContainerFactory} — for {@link DocumentUploadedEvent}
 *       payloads from the {@value KafkaTopics#DOCUMENT_UPLOADED} topic, enabling
 *       the OCR-driven AI analysis pipeline.</li>
 * </ul>
 * Each factory uses a custom {@link ObjectMapper} with {@link JavaTimeModule} for
 * consistent {@code LocalDateTime} deserialization without type header coupling.
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

    // -------------------------------------------------------------------------
    // ClaimCreatedEvent consumer factory
    // -------------------------------------------------------------------------

    /**
     * Constructs a typed {@link ConsumerFactory} for {@link ClaimCreatedEvent} payloads.
     *
     * @return a configured consumer factory for claim creation events
     */
    @Bean
    public ConsumerFactory<String, ClaimCreatedEvent> consumerFactory() {
        JsonDeserializer<ClaimCreatedEvent> deserializer =
                new JsonDeserializer<>(ClaimCreatedEvent.class, buildObjectMapper());
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(buildConsumerConfig(), new StringDeserializer(), deserializer);
    }

    /**
     * Constructs the primary {@link ConcurrentKafkaListenerContainerFactory} for
     * {@link ClaimCreatedEvent} listeners.
     *
     * @return a configured listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClaimCreatedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ClaimCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // -------------------------------------------------------------------------
    // DocumentUploadedEvent consumer factory
    // -------------------------------------------------------------------------

    /**
     * Constructs a typed {@link ConsumerFactory} for {@link DocumentUploadedEvent} payloads,
     * enabling the document-upload-triggered AI analysis pipeline.
     *
     * @return a configured consumer factory for document upload events
     */
    @Bean
    public ConsumerFactory<String, DocumentUploadedEvent> documentUploadedConsumerFactory() {
        JsonDeserializer<DocumentUploadedEvent> deserializer =
                new JsonDeserializer<>(DocumentUploadedEvent.class, buildObjectMapper());
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(buildConsumerConfig(), new StringDeserializer(), deserializer);
    }

    /**
     * Constructs the {@link ConcurrentKafkaListenerContainerFactory} for
     * {@link DocumentUploadedEvent} listeners.
     *
     * @return a configured listener container factory for document upload events
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DocumentUploadedEvent>
            documentUploadedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DocumentUploadedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(documentUploadedConsumerFactory());
        return factory;
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> buildConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return config;
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
}
