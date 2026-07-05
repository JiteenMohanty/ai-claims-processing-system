package com.jiteen.claims.claim.config;

/**
 * Centralized registry of Kafka topic name constants used across the
 * Claim Service event-driven communication boundaries.
 *
 * <p>
 * This class provides a single source of truth for all topic identifiers,
 * preventing magic string literals from being scattered across producers
 * and consumers. Both the event publisher and listener components reference
 * these constants, ensuring consistent topic routing throughout the platform.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    /**
     * Topic on which the Claim Service publishes new claim submission events.
     * Consumed by the AI Processing Service to trigger document analysis.
     */
    public static final String CLAIM_CREATED = "claim-created-topic";

    /**
     * Topic on which the AI Processing Service publishes completed analysis results.
     * Consumed by the Claim Service to store AI results and advance the claim lifecycle.
     */
    public static final String AI_ANALYSIS_COMPLETED = "ai-analysis-completed-topic";

    /**
     * Topic on which the Claim Service publishes claim approval events.
     * Consumed by the Notification Service to dispatch approval communications.
     */
    public static final String CLAIM_APPROVED = "claim-approved-topic";

    /**
     * Topic on which the Claim Service publishes claim rejection events.
     * Consumed by the Notification Service to dispatch rejection communications.
     */
    public static final String CLAIM_REJECTED = "claim-rejected-topic";

    /**
     * Topic on which the Claim Service publishes document upload events.
     * Consumed by the AI Processing Service to trigger OCR and document analysis
     * against the uploaded file stored in S3.
     */
    public static final String DOCUMENT_UPLOADED = "document-uploaded-topic";
}
