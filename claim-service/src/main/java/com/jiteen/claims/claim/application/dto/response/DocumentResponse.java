package com.jiteen.claims.claim.application.dto.response;

import com.jiteen.claims.claim.domain.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing the standard metadata response configuration
 * for an insurance claim attachment file resource.
 * <p>
 * This DTO is systematically populated and returned by resource retrieval APIs such as
 * {@code GET /api/claims/{claimId}/documents} and {@code GET /api/documents/{documentId}}.
 * It exposes all necessary business-relevant metadata definitions required by consumers and 
 * front-end rendering layers while explicitly encapsulating and masking underlying physical 
 * infrastructure components (such as local storage paths, random file hashing tokens, or soft-deletion maps).
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
public class DocumentResponse {

    /**
     * Unique relational primary key surrogate identifier of the document metadata record.
     */
    private Long documentId;

    /**
     * Unique operational transaction tracing identifier tracking the parent insurance 
     * claim resource associated with this supporting file attachment.
     */
    private UUID claimId;

    /**
     * The original, un-sanitized string filename of the document asset exactly as it was 
     * uploaded by the customer web ingress layer.
     */
    private String originalFileName;

    /**
     * Standardized Multipurpose Internet Mail Extensions (MIME) technical media type 
     * specification string detailing the payload structure formatting.
     */
    private String contentType;

    /**
     * Absolute numerical calculation measuring the data footprint size of the physical 
     * binary asset on disk or cloud object bucket storage, expressed in bytes.
     */
    private Long fileSize;

    /**
     * Current state machine processing workflow status variable assigned to this attachment 
     * asset within the asynchronous ingestion pipeline loop.
     */
    private DocumentStatus status;

    /**
     * Audit control timestamp tracking exactly when the attachment file record entry was 
     * initially instantiated and registered within the persistence layer.
     */
    private LocalDateTime createdAt;

    /**
     * Audit control timestamp recording the absolute last chronological modification applied 
     * against this document metadata resource row.
     */
    private LocalDateTime updatedAt;
}