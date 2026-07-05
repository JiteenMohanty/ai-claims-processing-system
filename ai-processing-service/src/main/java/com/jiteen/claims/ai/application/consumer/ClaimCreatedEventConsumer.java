package com.jiteen.claims.ai.application.consumer;

import com.jiteen.claims.ai.application.publisher.AiAnalysisEventPublisher;
import com.jiteen.claims.ai.application.service.AiAnalysisService;
import com.jiteen.claims.ai.config.KafkaTopics;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.ai.domain.event.ClaimCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer component that drives the AI analysis pipeline by consuming
 * {@link ClaimCreatedEvent} messages from the Claim Service.
 *
 * <p>
 * Upon receiving a {@link ClaimCreatedEvent} from the {@value KafkaTopics#CLAIM_CREATED}
 * topic, this consumer delegates the full analysis workflow to the
 * {@link AiAnalysisService} and then publishes the structured result as an
 * {@link AiAnalysisCompletedEvent} via the {@link AiAnalysisEventPublisher}.
 * </p>
 *
 * <p>
 * Error isolation: if the analysis pipeline throws an exception for a specific
 * claim, the failure is logged at ERROR level and the consumer continues
 * processing subsequent messages, preventing a single failed claim from
 * blocking the entire pipeline.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimCreatedEventConsumer {

    private final AiAnalysisService aiAnalysisService;
    private final AiAnalysisEventPublisher aiAnalysisEventPublisher;

    /**
     * Consumes a {@link ClaimCreatedEvent} from the {@value KafkaTopics#CLAIM_CREATED}
     * topic and orchestrates the AI analysis pipeline for the referenced claim.
     *
     * <p>
     * The full workflow executes synchronously within this handler method:
     * <ol>
     *   <li>Delegate analysis to {@link AiAnalysisService}</li>
     *   <li>Publish the {@link AiAnalysisCompletedEvent} result</li>
     * </ol>
     * Any exception thrown during analysis is caught, logged, and swallowed
     * to prevent consumer offset advancement failure and downstream batch blocking.
     * </p>
     *
     * @param event the deserialized {@link ClaimCreatedEvent} received from Kafka
     */
    @KafkaListener(
        topics = KafkaTopics.CLAIM_CREATED,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClaimCreated(@Payload ClaimCreatedEvent event) {
        log.info("Received ClaimCreatedEvent — claimId: {}, claimType: {}, policy: {}",
                event.getClaimId(), event.getClaimType(), event.getPolicyNumber());

        try {
            AiAnalysisCompletedEvent result = aiAnalysisService.analyzeCliam(event);
            aiAnalysisEventPublisher.publishAnalysisCompleted(result);
        } catch (Exception ex) {
            log.error("AI analysis pipeline failed for claimId: {} — {}",
                    event.getClaimId(), ex.getMessage(), ex);
        }
    }
}
