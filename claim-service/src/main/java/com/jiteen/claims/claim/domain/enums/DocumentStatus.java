package com.jiteen.claims.claim.domain.enums;

/**
 * Represents the lifecycle state of an uploaded claim document within the
 * AI-Powered Insurance Claims Processing Platform.
 * <p>
 * Document lifecycle management is critical to ensure data consistency, trace asynchronously
 * executed AI operations (such as OCR extraction, content classification, and fraud detection),
 * and manage storage cleanup pipelines safely. This enum transitions along with the 
 * document processing stages from initial ingestion to eventual terminal state or archival.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public enum DocumentStatus {

    /**
     * The document has been successfully uploaded by the client, persisted 
     * within the object storage system, and its tracking metadata has been recorded.
     */
    UPLOADED,

    /**
     * The document is currently undergoing asynchronous processing, such as text 
     * extraction via OCR, semantic document classification, or automated layout analysis.
     */
    PROCESSING,

    /**
     * The document processing pipeline has completed successfully. Structured text, 
     * content verification matrices, and metadata summaries have been extracted.
     */
    PROCESSED,

    /**
     * The processing pipeline encountered an unrecoverable error, such as a corrupted 
     * file format, unreadable scan layers, or infrastructure timeout anomalies.
     */
    FAILED,

    /**
     * The document has been flagged as soft-deleted by the consumer or claim officer, 
     * isolating it from operational views and queuing it for long-term data retention archival.
     */
    DELETED
}