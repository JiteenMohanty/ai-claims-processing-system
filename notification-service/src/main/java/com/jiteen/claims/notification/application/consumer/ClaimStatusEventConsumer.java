package com.jiteen.claims.notification.application.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jiteen.claims.notification.application.service.EmailNotificationService;
import com.jiteen.claims.notification.config.KafkaTopics;
import com.jiteen.claims.notification.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.notification.domain.event.ClaimRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer component that processes claim lifecycle status events and
 * dispatches the corresponding customer-facing email notifications.
 *
 * <p>
 * This consumer subscribes to both the {@value KafkaTopics#CLAIM_APPROVED} and
 * {@value KafkaTopics#CLAIM_REJECTED} topics within the same consumer group.
 * Each received event is deserialized from its JSON payload and routed to the
 * {@link EmailNotificationService} for notification dispatch.
 * </p>
 *
 * <p>
 * Error isolation: exceptions during individual message processing are caught
 * and logged at ERROR level. The consumer continues processing subsequent messages
 * to prevent a single notification failure from blocking the entire pipeline.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimStatusEventConsumer {

    private final EmailNotificationService emailNotificationService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * Consumes and processes a claim approval event, dispatching an email
     * notification to the claimant confirming their claim approval outcome.
     *
     * @param record the raw Kafka consumer record from the claim-approved topic
     */
    @KafkaListener(
        topics = KafkaTopics.CLAIM_APPROVED,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClaimApproved(ConsumerRecord<String, Object> record) {
        log.info("Received ClaimApprovedEvent from topic: {}, partition: {}, offset: {}",
                record.topic(), record.partition(), record.offset());

        try {
            ClaimApprovedEvent event = OBJECT_MAPPER.convertValue(record.value(), ClaimApprovedEvent.class);
            log.info("Processing approval notification for claimId: {}", event.getClaimId());
            emailNotificationService.sendClaimApprovedNotification(event);
        } catch (Exception ex) {
            log.error("Failed to process ClaimApprovedEvent from offset {}: {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    /**
     * Consumes and processes a claim rejection event, dispatching an email
     * notification to the claimant communicating the rejection outcome and
     * providing guidance for appeal procedures.
     *
     * @param record the raw Kafka consumer record from the claim-rejected topic
     */
    @KafkaListener(
        topics = KafkaTopics.CLAIM_REJECTED,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClaimRejected(ConsumerRecord<String, Object> record) {
        log.info("Received ClaimRejectedEvent from topic: {}, partition: {}, offset: {}",
                record.topic(), record.partition(), record.offset());

        try {
            ClaimRejectedEvent event = OBJECT_MAPPER.convertValue(record.value(), ClaimRejectedEvent.class);
            log.info("Processing rejection notification for claimId: {}", event.getClaimId());
            emailNotificationService.sendClaimRejectedNotification(event);
        } catch (Exception ex) {
            log.error("Failed to process ClaimRejectedEvent from offset {}: {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }
}
