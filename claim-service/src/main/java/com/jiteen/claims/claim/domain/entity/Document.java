package com.jiteen.claims.claim.domain.entity;

import com.jiteen.claims.claim.domain.enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Persistence Entity representing an uploaded supporting document attached to an insurance claim
 * within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This class maps directly to the 'documents' database table in PostgreSQL. It captures 
 * critical metadata regarding customer-submitted attachments (e.g., invoices, police reports, medical files). 
 * Documents are initially tracked on local disk during Phase 4 implementation and are architected to cleanly 
 * transition to an AWS S3 cloud object storage architecture in subsequent phases without schema disruption.
 * </p>
 * <p>
 * This entity inherits comprehensive system-managed auditing and transparent soft-delete capabilities 
 * by extending the {@code AuditBaseEntity} class platform standard.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document extends AuditBaseEntity {

    /**
     * Unique surrogate primary key identification tracking reference for the persisted document record.
     * Uses an autoincrementing Bigint sequence generation strategy mapped via identity columns.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * The parent insurance claim transaction that this supporting document validates.
     * Established as a mandatory, lazy-loaded relation to minimize memory footprints during bulk updates.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    /**
     * The authentic, un-sanitized name of the file exactly as it was originally submitted 
     * by the consumer web ingress layer (e.g., "medical_report_01.pdf").
     */
    @Column(name = "original_file_name", length = 255, nullable = false)
    private String originalFileName;

    /**
     * The unique, randomized, system-generated alphanumeric name assigned to the file upon system 
     * ingestion to avoid file storage collisions and directory parsing risks.
     */
    @Column(name = "stored_file_name", length = 255, nullable = false)
    private String storedFileName;

    /**
     * Standardized Multipurpose Internet Mail Extensions (MIME) technical format type identifier 
     * used to establish validation boundaries (e.g., "application/pdf", "image/png").
     */
    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    /**
     * Absolute numerical volume size measurement of the binary document expressed in bytes.
     * Crucial parameter used for validating global platform storage quota limits.
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * Fully qualified local filesystem path or future cloud S3 URI locator reference identifying exactly 
     * where the physical binary asset is stored inside the infrastructure layer.
     */
    @Column(name = "storage_path", length = 1000, nullable = false)
    private String storagePath;

    /**
     * State machine indicator monitoring the synchronization loop phase of the document asset.
     * Persisted strictly as an upper-case text string value matching database constraint metrics.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", length = 50, nullable = false)
    private DocumentStatus status;
}