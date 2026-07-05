package com.jiteen.claims.claim.domain.repository;

import com.jiteen.claims.claim.domain.entity.AiAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository providing data access operations for
 * {@link AiAnalysisResult} entity records within the AI analysis persistence layer.
 *
 * <p>
 * This repository supports lookup of AI analysis results by claim identifier,
 * enabling the Claim Service to retrieve AI-generated intelligence for a given
 * insurance claim without requiring a join to the claims table at the repository level.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Repository
public interface AiAnalysisResultRepository extends JpaRepository<AiAnalysisResult, UUID> {

    /**
     * Retrieves the AI analysis result record associated with the specified claim identifier.
     *
     * @param claimId the unique {@link UUID} identifier of the parent insurance claim
     * @return an {@link Optional} containing the {@link AiAnalysisResult} if found,
     *         or empty if no analysis has been completed for the given claim
     */
    Optional<AiAnalysisResult> findByClaimId(UUID claimId);

    /**
     * Determines whether an AI analysis result record already exists for the specified claim.
     *
     * @param claimId the unique {@link UUID} identifier of the parent insurance claim
     * @return {@code true} if an analysis result exists; {@code false} otherwise
     */
    boolean existsByClaimId(UUID claimId);
}
