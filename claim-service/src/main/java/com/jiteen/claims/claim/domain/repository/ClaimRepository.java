package com.jiteen.claims.claim.domain.repository;

import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object (DAO) layer interface for managing {@link Claim} domain
 * persistence entities.
 * Provides standardized operations for data creation,
 * retrieval, update, and analytics reporting
 * against the underlying relational system of record.
 * *
 * <p>
 * This repository mirrors the exact enterprise design principles established
 * within the
 * platform's core Authentication microservice[cite: 3]. It natively supports a
 * soft-delete data
 * architecture across all query operations by strictly filtering out rows
 * possessing a populated
 * deletion timestamp (where {@code deletedAt IS NULL}).
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    /**
     * Retrieves an active, non-deleted claim record uniquely identified by its
     * database primary key.
     *
     * @param id the unique {@link UUID} tracking the target insurance claim
     * @return an {@link Optional} containing the active {@link Claim} entity if
     *         present,
     *         or an empty {@link Optional} if the record does not exist or has been
     *         soft-deleted
     */
    Optional<Claim> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Verifies the existence of a valid, active claim record matching the specified
     * identifier.
     *
     * @param id the unique {@link UUID} tracking the target insurance claim
     * @return {@code true} if a matching non-deleted claim is found; {@code false}
     *         otherwise
     */
    boolean existsByIdAndDeletedAtIsNull(UUID id);

    /**
     * Retrieves all active, non-deleted claims currently transitioning through a
     * specific lifecycle state.
     * This method supports batch orchestration, automated workflow routing, and
     * business reporting pipelines
     *
     * @param status the targeted {@link ClaimStatus} workflow enum phase
     * @return a {@link List} of all matching non-deleted {@link Claim} entities; an
     *         empty list if none match
     */
    List<Claim> findByStatusAndDeletedAtIsNull(ClaimStatus status);

    /**
     * Locates all active, non-deleted claims associated with an explicit business
     * policy contract reference.
     *
     * @param policyNumber the unique string identifying the customer's coverage
     *                     policy
     * @return a {@link List} containing active {@link Claim} models associated with
     *         the given policy contract
     */
    List<Claim> findByPolicyNumberAndDeletedAtIsNull(String policyNumber);

    /**
     * Executes a case-insensitive fractional wild-card match against claimant names
     * to locate corresponding
     * active, non-deleted claim records. Supports wild-card filtering for
     * administrative query operations.
     *
     * @param claimantName the full or partial text string representing the
     *                     customer's name
     * @return a {@link List} of active {@link Claim} entities aligned with the text
     *         filtering token
     */
    List<Claim> findByClaimantNameContainingIgnoreCaseAndDeletedAtIsNull(String claimantName);

    /**
     * Calculates the aggregate volume of active, non-deleted claims currently
     * resting within a given state.
     * This utility enables operational platform metrics tracking, SLA dashboard
     * compliance monitoring, and analytics reporting[cite: 36].
     *
     * @param status the target {@link ClaimStatus} lifecycle state to run
     *               calculations for
     * @return a long primitive value tracking the total number of claims evaluating
     *         in the requested status
     */
    long countByStatusAndDeletedAtIsNull(ClaimStatus status);

    List<Claim> findAllByDeletedAtIsNull();
    
    List<Claim> findByDeletedAtIsNull();
}