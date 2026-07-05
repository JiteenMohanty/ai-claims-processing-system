package com.jiteen.claims.claim.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a supporting document is successfully uploaded
 * and persisted for an insurance claim.
 *
 * <p>
 * This event carries the storage location reference (S3 key or local path)
 * alongside document metadata, enabling the AI Processing Service to retrieve
 * the binary asset via Textract or the configured OCR provider. The
 * {@code storageProvider} field tells consumers whether the file is accessible
 * via AWS S3 (eligible for Textract OCR) or is stored locally (simulation
 * fallback only).
 * </p>
 *
 * <p>
 * Published to the {@code document-uploaded-topic} Kafka topic immediately
 * after the document record is committed to the database.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadedEvent {

    /**
     * UUID of the parent insurance claim this document belongs to.
     */
    private UUID claimId;

    /**
     * Database ID of the persisted document record.
     */
    private Long documentId;

    /**
     * The storage path: an S3 object key (e.g., {@code claims/uuid.pdf}) when
     * using S3 storage, or an absolute filesystem path when using local storage.
     */
    private String storagePath;

    /**
     * Identifier of the active storage backend. Values: {@code "s3"} or {@code "local"}.
     * Used by the AI Processing Service to determine whether Textract OCR is applicable.
     */
    private String storageProvider;

    /**
     * The original filename as submitted by the client (e.g., {@code medical_report.pdf}).
     */
    private String originalFileName;

    /**
     * MIME type of the uploaded file (e.g., {@code application/pdf}, {@code image/jpeg}).
     */
    private String contentType;

    /**
     * Timestamp at which the document was uploaded and this event was constructed.
     */
    private LocalDateTime uploadedAt;
}
