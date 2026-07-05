package com.jiteen.claims.ai.config;

/**
 * Centralized registry of Kafka topic name constants used within the
 * AI Processing Service messaging boundary.
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    /**
     * Topic from which the AI Processing Service consumes new claim submission events.
     * Published by the Claim Service upon successful claim creation.
     */
    public static final String CLAIM_CREATED = "claim-created-topic";

    /**
     * Topic on which the AI Processing Service publishes completed analysis results.
     * Consumed by the Claim Service to advance the claim lifecycle.
     */
    public static final String AI_ANALYSIS_COMPLETED = "ai-analysis-completed-topic";

    /**
     * Topic from which the AI Processing Service consumes document upload events.
     * Published by the Claim Service after a supporting document is stored, allowing
     * the AI pipeline to retrieve the binary asset via Textract for OCR analysis.
     */
    public static final String DOCUMENT_UPLOADED = "document-uploaded-topic";
}
