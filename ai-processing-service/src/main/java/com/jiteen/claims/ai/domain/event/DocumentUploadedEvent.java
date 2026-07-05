package com.jiteen.claims.ai.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inbound domain event consumed by the AI Processing Service when a new
 * supporting document has been uploaded and persisted by the Claim Service.
 *
 * <p>
 * The {@code storagePath} carries the S3 object key (when
 * {@code storageProvider="s3"}) or a local filesystem path (when
 * {@code storageProvider="local"}). Only S3-backed documents are eligible
 * for Amazon Textract OCR; local-backed documents fall back to simulation.
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
     * Database ID of the persisted document record in the Claim Service.
     */
    private Long documentId;

    /**
     * The storage path: S3 object key or local filesystem path.
     */
    private String storagePath;

    /**
     * Storage backend identifier: {@code "s3"} or {@code "local"}.
     */
    private String storageProvider;

    /**
     * Original filename as submitted by the client.
     */
    private String originalFileName;

    /**
     * MIME type of the uploaded file.
     */
    private String contentType;

    /**
     * Timestamp at which the document was uploaded.
     */
    private LocalDateTime uploadedAt;
}
