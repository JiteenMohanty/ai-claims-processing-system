package com.jiteen.claims.claim.application.event;

import com.jiteen.claims.claim.config.CacheNames;
import com.jiteen.claims.claim.config.KafkaTopics;
import com.jiteen.claims.claim.domain.entity.AiAnalysisResult;
import com.jiteen.claims.claim.domain.entity.ClaimStatusHistory;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import com.jiteen.claims.claim.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.claim.domain.repository.AiAnalysisResultRepository;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import com.jiteen.claims.claim.domain.repository.ClaimStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kafka consumer component responsible for processing incoming event messages
 * from the AI Processing Service within the Claim Service event-driven pipeline.
 *
 * <p>
 * This consumer subscribes to the {@value KafkaTopics#AI_ANALYSIS_COMPLETED} topic
 * and performs the following operations upon receiving an analysis completion event:
 * </p>
 * <ol>
 *   <li>Locates the parent claim record in the persistence layer.</li>
 *   <li>Advances the claim lifecycle status to {@link ClaimStatus#AI_REVIEW_COMPLETED}.</li>
 *   <li>Records the status transition in the {@code claim_status_history} audit trail.</li>
 *   <li>Persists the structured AI analysis result to the {@code ai_analysis_results} table.</li>
 * </ol>
 *
 * <p>
 * All operations execute within a single database transaction to ensure consistency
 * between the claim status update, audit history record, and analysis result persistence.
 * Failed message processing is logged at ERROR level for operational alerting.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventConsumer {

    private static final String AI_PROCESSOR = "AI_PROCESSING_SERVICE";

    private final ClaimRepository claimRepository;
    private final AiAnalysisResultRepository aiAnalysisResultRepository;
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;
    private final CacheManager cacheManager;

    /**
     * Consumes {@link AiAnalysisCompletedEvent} payloads from the
     * {@value KafkaTopics#AI_ANALYSIS_COMPLETED} topic and orchestrates the
     * downstream persistence and claim lifecycle advancement workflows.
     *
     * @param event the deserialized {@link AiAnalysisCompletedEvent} payload
     *              received from the AI Processing Service
     */
    @KafkaListener(
        topics = KafkaTopics.AI_ANALYSIS_COMPLETED,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleAiAnalysisCompleted(@Payload AiAnalysisCompletedEvent event) {
        log.info("Received AiAnalysisCompletedEvent for claimId: {} with riskScore: {}",
                event.getClaimId(), event.getRiskScore());

        claimRepository.findByIdAndDeletedAtIsNull(event.getClaimId()).ifPresentOrElse(claim -> {

            ClaimStatus previousStatus = claim.getStatus();

            claim.setStatus(ClaimStatus.AI_REVIEW_COMPLETED);
            claimRepository.save(claim);

            ClaimStatusHistory history = ClaimStatusHistory.builder()
                    .claimId(event.getClaimId())
                    .oldStatus(previousStatus)
                    .newStatus(ClaimStatus.AI_REVIEW_COMPLETED)
                    .changedBy(AI_PROCESSOR)
                    .changedAt(LocalDateTime.now())
                    .build();
            claimStatusHistoryRepository.save(history);

            if (!aiAnalysisResultRepository.existsByClaimId(event.getClaimId())) {
                AiAnalysisResult result = AiAnalysisResult.builder()
                        .claimId(event.getClaimId())
                        .summary(event.getSummary())
                        .riskScore(event.getRiskScore())
                        .recommendedAction(event.getRecommendedAction())
                        .fraudIndicators(event.getFraudIndicators())
                        .policyNumber(event.getPolicyNumberExtracted())
                        .customerNameExtracted(event.getCustomerNameExtracted())
                        .missingDocuments(event.getMissingDocuments())
                        .processedAt(event.getProcessedAt())
                        .build();

                aiAnalysisResultRepository.save(result);
            }

            // Evict stale claim entry and list from Redis cache
            Cache claimsCache = cacheManager.getCache(CacheNames.CLAIMS);
            if (claimsCache != null) {
                claimsCache.evict(event.getClaimId());
            }
            Cache claimsListCache = cacheManager.getCache(CacheNames.CLAIMS_LIST);
            if (claimsListCache != null) {
                claimsListCache.clear();
            }

            log.info("AI analysis result persisted and claim status advanced to AI_REVIEW_COMPLETED for claimId: {}",
                    event.getClaimId());

        }, () -> log.warn("Received AiAnalysisCompletedEvent for unknown or deleted claimId: {}",
                event.getClaimId()));
    }
}
