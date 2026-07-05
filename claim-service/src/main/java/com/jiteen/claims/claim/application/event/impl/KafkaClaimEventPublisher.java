package com.jiteen.claims.claim.application.event.impl;

import com.jiteen.claims.claim.application.event.ClaimEventPublisher;
import com.jiteen.claims.claim.config.KafkaTopics;
import com.jiteen.claims.claim.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.claim.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.claim.domain.event.ClaimRejectedEvent;
import com.jiteen.claims.claim.domain.event.DocumentUploadedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Apache Kafka implementation of the {@link ClaimEventPublisher} contract.
 *
 * <p>
 * This component serializes claim lifecycle event payloads as JSON and routes
 * them to the configured Kafka topics using the Spring {@link KafkaTemplate}.
 * Each publish operation is asynchronous; success and failure outcomes are
 * captured via callback logging to maintain observability without blocking the
 * calling service thread.
 * </p>
 *
 * <p>
 * Producer reliability is enforced through {@code acks=all} and idempotent
 * producer settings configured in {@code application.yml}, ensuring no message
 * loss under broker restarts or network partitions.
 * </p>
 *
 * <p>
 * <strong>Phase 9 — Resilience:</strong> Each publish method is decorated with
 * a {@link CircuitBreaker} and {@link Retry} to protect against Kafka broker
 * unavailability. If the broker is unreachable at send time, the circuit breaker
 * opens after the configured failure threshold and routes calls to a fallback
 * that logs the dropped event. Retries with exponential back-off handle transient
 * failures before the circuit trips.
 * </p>
 *
 * @author Jiteen
 * @version 2.0
 * @since Java 21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClaimEventPublisher implements ClaimEventPublisher {

    private static final String CB_NAME = "kafkaPublisher";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Sends the event to {@value KafkaTopics#CLAIM_CREATED}, keyed by the
     * string representation of the claim UUID to preserve ordering for all
     * events belonging to the same claim.
     * </p>
     */
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackPublishClaimCreated")
    @Retry(name = CB_NAME)
    public void publishClaimCreated(ClaimCreatedEvent event) {
        log.info("Publishing ClaimCreatedEvent for claimId: {}", event.getClaimId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.CLAIM_CREATED, event.getClaimId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish ClaimCreatedEvent for claimId: {} — {}",
                        event.getClaimId(), ex.getMessage(), ex);
            } else {
                log.debug("ClaimCreatedEvent published successfully for claimId: {} — partition: {}, offset: {}",
                        event.getClaimId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Sends the event to {@value KafkaTopics#CLAIM_APPROVED}, keyed by the
     * string representation of the claim UUID to preserve ordering for all
     * events belonging to the same claim.
     * </p>
     */
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackPublishClaimApproved")
    @Retry(name = CB_NAME)
    public void publishClaimApproved(ClaimApprovedEvent event) {
        log.info("Publishing ClaimApprovedEvent for claimId: {}", event.getClaimId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.CLAIM_APPROVED, event.getClaimId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish ClaimApprovedEvent for claimId: {} — {}",
                        event.getClaimId(), ex.getMessage(), ex);
            } else {
                log.debug("ClaimApprovedEvent published successfully for claimId: {} — partition: {}, offset: {}",
                        event.getClaimId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Sends the event to {@value KafkaTopics#CLAIM_REJECTED}, keyed by the
     * string representation of the claim UUID to preserve ordering for all
     * events belonging to the same claim.
     * </p>
     */
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackPublishClaimRejected")
    @Retry(name = CB_NAME)
    public void publishClaimRejected(ClaimRejectedEvent event) {
        log.info("Publishing ClaimRejectedEvent for claimId: {}", event.getClaimId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.CLAIM_REJECTED, event.getClaimId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish ClaimRejectedEvent for claimId: {} — {}",
                        event.getClaimId(), ex.getMessage(), ex);
            } else {
                log.debug("ClaimRejectedEvent published successfully for claimId: {} — partition: {}, offset: {}",
                        event.getClaimId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Sends the event to {@value KafkaTopics#DOCUMENT_UPLOADED}, keyed by the
     * string representation of the claim UUID so all document events for the
     * same claim land on the same partition in arrival order.
     * </p>
     */
    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackPublishDocumentUploaded")
    @Retry(name = CB_NAME)
    public void publishDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Publishing DocumentUploadedEvent for claimId: {}, documentId: {}",
                event.getClaimId(), event.getDocumentId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.DOCUMENT_UPLOADED, event.getClaimId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish DocumentUploadedEvent for claimId: {}, documentId: {} — {}",
                        event.getClaimId(), event.getDocumentId(), ex.getMessage(), ex);
            } else {
                log.debug("DocumentUploadedEvent published successfully for claimId: {} — partition: {}, offset: {}",
                        event.getClaimId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Circuit-breaker fallback methods
    // -------------------------------------------------------------------------

    /**
     * Fallback invoked when the circuit is open or all retries are exhausted
     * while publishing a {@link ClaimCreatedEvent}.
     *
     * @param event     the event that could not be delivered
     * @param throwable the root cause that tripped the circuit
     */
    private void fallbackPublishClaimCreated(ClaimCreatedEvent event, Throwable throwable) {
        log.error("[CIRCUIT OPEN] Unable to publish ClaimCreatedEvent for claimId: {}. " +
                  "Kafka unavailable — downstream AI analysis will not be triggered. Cause: {}",
                  event.getClaimId(), throwable.getMessage());
    }

    /**
     * Fallback invoked when the circuit is open or all retries are exhausted
     * while publishing a {@link ClaimApprovedEvent}.
     *
     * @param event     the event that could not be delivered
     * @param throwable the root cause that tripped the circuit
     */
    private void fallbackPublishClaimApproved(ClaimApprovedEvent event, Throwable throwable) {
        log.error("[CIRCUIT OPEN] Unable to publish ClaimApprovedEvent for claimId: {}. " +
                  "Notification Service will not be alerted. Cause: {}",
                  event.getClaimId(), throwable.getMessage());
    }

    /**
     * Fallback invoked when the circuit is open or all retries are exhausted
     * while publishing a {@link ClaimRejectedEvent}.
     *
     * @param event     the event that could not be delivered
     * @param throwable the root cause that tripped the circuit
     */
    private void fallbackPublishClaimRejected(ClaimRejectedEvent event, Throwable throwable) {
        log.error("[CIRCUIT OPEN] Unable to publish ClaimRejectedEvent for claimId: {}. " +
                  "Notification Service will not be alerted. Cause: {}",
                  event.getClaimId(), throwable.getMessage());
    }

    /**
     * Fallback invoked when the circuit is open or all retries are exhausted
     * while publishing a {@link DocumentUploadedEvent}.
     *
     * @param event     the event that could not be delivered
     * @param throwable the root cause that tripped the circuit
     */
    private void fallbackPublishDocumentUploaded(DocumentUploadedEvent event, Throwable throwable) {
        log.error("[CIRCUIT OPEN] Unable to publish DocumentUploadedEvent for claimId: {}, documentId: {}. " +
                  "OCR analysis pipeline will not be triggered. Cause: {}",
                  event.getClaimId(), event.getDocumentId(), throwable.getMessage());
    }
}
