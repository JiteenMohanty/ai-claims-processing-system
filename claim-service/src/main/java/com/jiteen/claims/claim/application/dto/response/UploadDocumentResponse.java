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
 * Data Transfer Object (DTO) representing the acknowledgment payload returned to clients
 * upon a successful document upload transaction within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This class abstracts the core metadata particulars of an ingested supporting file attachment, 
 * delivering critical integration tracking markers to edge layers while safely encapsulating and 
 * hiding underlying filesystem mechanics (such as internal disk directories, structural server paths, 
 * or unique object storage hashing aliases).
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
public class UploadDocumentResponse {

    /**
     * Unique relational database surrogate key identifier allocated by the persistence 
     * engine to track this specific document record.
     */
    private Long documentId;

    /**
     * Unique enterprise transaction tracking identifier of the parent insurance claim 
     * resource to which this document asset has been successfully associated.
     */
    private UUID claimId;

    /**
     * The authentic, un-sanitized literal name of the file exactly as it was compiled 
     * and dispatched by the end-user client interface tier.
     */
    private String originalFileName;

    /**
     * Standardized Multipurpose Internet Mail Extensions (MIME) technical media format type 
     * string used to guide downstream secure payload processing strategies.
     */
    private String contentType;

    /**
     * Absolute physical data volume footprint configuration of the binary file asset, 
     * explicitly measured and represented in bytes.
     */
    private Long fileSize;

    /**
     * Current workflow orchestration phase state of the attachment within the system 
     * ingestion pipeline, mapping directly to structural processing status matrices.
     */
    private DocumentStatus status;

    /**
     * System audit timestamp denoting the absolute chronological point in time when the 
     * file asset transaction was formally initialized and registered within the infrastructure store.
     */
    private LocalDateTime uploadedAt;
}