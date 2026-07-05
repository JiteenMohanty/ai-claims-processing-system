package com.jiteen.claims.ai.application.publisher.impl;

import com.jiteen.claims.ai.application.publisher.AiAnalysisEventPublisher;
import com.jiteen.claims.ai.config.KafkaTopics;
import com.jiteen.claims.ai.domain.event.AiAnalysisCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Apache Kafka implementation of the {@link AiAnalysisEventPublisher} contract.
 *
 * <p>
 * Serializes {@link AiAnalysisCompletedEvent} payloads as JSON and routes them
 * to the {@value KafkaTopics#AI_ANALYSIS_COMPLETED} topic. Publish operations
 * are asynchronous; success and failure outcomes are captured via callback logging
 * to maintain observability without blocking the analysis thread.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAiAnalysisEventPublisher implements AiAnalysisEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Sends the event to {@value KafkaTopics#AI_ANALYSIS_COMPLETED}, keyed by
     * the string representation of the claim UUID to preserve message ordering
     * for all events associated with the same claim.
     * </p>
     */
    @Override
    public void publishAnalysisCompleted(AiAnalysisCompletedEvent event) {
        log.info("Publishing AiAnalysisCompletedEvent for claimId: {} with riskScore: {}",
                event.getClaimId(), event.getRiskScore());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.AI_ANALYSIS_COMPLETED,
                        event.getClaimId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish AiAnalysisCompletedEvent for claimId: {} — {}",
                        event.getClaimId(), ex.getMessage(), ex);
            } else {
                log.debug("AiAnalysisCompletedEvent published for claimId: {} — partition: {}, offset: {}",
                        event.getClaimId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
