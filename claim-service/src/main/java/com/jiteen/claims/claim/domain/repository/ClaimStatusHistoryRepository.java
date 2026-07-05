package com.jiteen.claims.claim.domain.repository;

import com.jiteen.claims.claim.domain.entity.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository providing data access operations for
 * {@link ClaimStatusHistory} audit trail records within the claims processing domain.
 *
 * <p>
 * Status history records represent an immutable append-only audit log.
 * This repository exposes ordered retrieval operations to support claims
 * officers reviewing the complete lifecycle transition sequence of a given claim.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Repository
public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {

    /**
     * Retrieves all status transition records for the specified claim, ordered
     * chronologically from the earliest to the most recent transition.
     *
     * @param claimId the unique {@link UUID} identifier of the parent insurance claim
     * @return an ordered {@link List} of {@link ClaimStatusHistory} records;
     *         empty if no history exists for the claim
     */
    List<ClaimStatusHistory> findByClaimIdOrderByChangedAtAsc(UUID claimId);
}
