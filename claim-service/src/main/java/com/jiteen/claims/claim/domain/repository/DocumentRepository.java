package com.jiteen.claims.claim.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jiteen.claims.claim.domain.entity.Document;
import com.jiteen.claims.claim.domain.enums.DocumentStatus;

/**
 * Data Access Object (DAO) layer interface for managing {@link Document} persistence lifecycle entities.
 * Provides abstracted relational database operations against the 'documents' database table.
 * <p>
 * This repository handles metadata management for files uploaded to validate and back up active insurance claims.
 * It strictly follows the engineering standards established within the platform's architectural blueprint,
 * supporting derived query orchestration for high-performance retrieval patterns.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Retrieves all supporting document entries linked to a specific insurance claim transaction.
     * <p>
     * This method is essential for compiling aggregate evidence file attachments during manual adjustments,
     * AI-assisted fraud detection routines, and document cross-referencing loops.
     * </p>
     *
     * @param claimId the unique {@link UUID} primary key tracking the parent insurance claim resource
     * @return a {@link List} containing matching {@link Document} metadata entities; an empty list if none match
     */
    // List<Document> findByClaimId(UUID claimId);

    /**
     * Extracts a historical log collection of all claim documents resting in a specific processing lifecycle phase.
     * <p>
     * Primarily utilized by asynchronous batch processing workflows, orchestration workers, or AI service triggers
     * filtering for tasks pending processing (e.g., status matching {@link DocumentStatus#UPLOADED} or {@link DocumentStatus#PROCESSING}).
     * </p>
     *
     * @param status the targeted {@link DocumentStatus} enum lifecycle state variable
     * @return a {@link List} of all matching {@link Document} entities aligned with the query criteria state
     */
    List<Document> findByStatus(DocumentStatus status);

    Optional<Document> findByIdAndDeletedAtIsNull(Long documentId);

    List<Document> findByClaimIdAndDeletedAtIsNull(UUID claimId);

    boolean existsByIdAndDeletedAtIsNull(Long id);
}