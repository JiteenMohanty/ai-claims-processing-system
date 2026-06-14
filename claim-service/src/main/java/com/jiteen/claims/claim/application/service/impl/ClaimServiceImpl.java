package com.jiteen.claims.claim.application.service.impl;

import com.jiteen.claims.claim.api.exception.ClaimNotFoundException;
import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.application.service.ClaimService;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * This class coordinates the core operational business logic for processing and
 * transitioning
 * insurance claims within the AI-Powered Insurance Claims Processing Platform.
 * It acts as the
 * bridge between the inbound REST API request controller tier and the
 * persistent storage repository layer.
 * </p>
 * <p>
 * Following enterprise standards established in the reference Authentication
 * service, this class
 * relies on declarative transaction demarcation, constructor-based dependency
 * injection via Lombok,
 * and explicit manual DTO-to-Entity decomposition mapping to isolate
 * infrastructure concerns.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;

    /**
     * Ingests and initializes a new insurance claim within the system of record.
     * <p>
     * Maps incoming request particulars to a clean domain model instance and forces
     * the initial workflow
     * tracking status to {@link ClaimStatus#SUBMITTED}. Operational identifier
     * allocation and audit trail
     * metadata properties remain unpopulated at this stage to allow JPA
     * orchestration assignment.
     * </p>
     *
     * @param request the {@link CreateClaimRequest} payload containing consumer and
     *                policy contract vectors
     * @return a structured {@link ClaimResponse} representing the newly registered
     *         and persisted claim entity
     */
    @Override
    public ClaimResponse createClaim(CreateClaimRequest request) {
        Claim claim = Claim.builder()
                .policyNumber(request.getPolicyNumber())
                .claimantName(request.getClaimantName())
                .claimType(request.getClaimType())
                .incidentDate(request.getIncidentDate())
                .claimAmount(request.getClaimAmount())
                .description(request.getDescription())
                .status(ClaimStatus.SUBMITTED)
                .build();

        Claim savedClaim = claimRepository.save(claim);
        return mapToResponse(savedClaim);
    }

    /**
     * Locates and retrieves an active, non-deleted insurance claim matching the
     * provided unique reference key.
     *
     * @param claimId the unique {@link UUID} key identifying the requested claim
     *                record entry
     * @return a fully populated {@link ClaimResponse} mapping the active entity
     *         data context
     * @throws ClaimNotFoundException if no matching active claim is found within the data
     *                          persistence layer
     */
    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
        return mapToResponse(claim);
    }

    /**
     * Compiles and maps a listing of all active, non-deleted insurance claims
     * stored in the database registry.
     * <p>
     * Leverages the Java Stream API to process data storage sets and applies a
     * layer filter to actively
     * discard records that have been soft-deleted by checking for the presence of a
     * deletion timestamp.
     * </p>
     *
     * @return a {@link List} of {@link ClaimResponse} objects representing all
     *         matching active claims;
     *         returns an empty list if no un-deleted records exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findByDeletedAtIsNull()
        .stream()
        .map(this::mapToResponse)
        .toList();
    }

    /**
     * Authorizes and approves an active insurance claim, moving its lifecycle to
     * the final completion loop.
     *
     * @param claimId the unique {@link UUID} key tracking the target insurance
     *                claim entry
     * @return an updated {@link ClaimResponse} showing the successful shift to the
     *         approved state matrix
     * @throws ClaimNotFoundException if the target claim cannot be found or has been
     *                          soft-deleted
     */
    @Override
    public ClaimResponse approveClaim(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        claim.setStatus(ClaimStatus.APPROVED);
        Claim updatedClaim = claimRepository.save(claim);
        return mapToResponse(updatedClaim);
    }

    /**
     * Formally denies and rejects an active insurance claim, locking it into a
     * terminal rejection state.
     *
     * @param claimId the unique {@link UUID} key tracking the target insurance
     *                claim entry
     * @return an updated {@link ClaimResponse} showing the permanent shift to the
     *         rejected state matrix
     * @throws ClaimNotFoundException if the target claim cannot be found or has been
     *                          soft-deleted
     */
    @Override
    public ClaimResponse rejectClaim(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        claim.setStatus(ClaimStatus.REJECTED);
        Claim updatedClaim = claimRepository.save(claim);
        return mapToResponse(updatedClaim);
    }

    /**
     * Decouples the persistent infrastructure context from the presentation
     * delivery tier by cleanly
     * mapping an internal entity instance to an immutable external presentation
     * transfer object.
     * <p>
     * This utility explicitly omits soft-delete fields such as {@code deletedAt} to
     * avoid leakage
     * of operational database details outside the service ecosystem boundary.
     * </p>
     *
     * @param claim the database-backed {@link Claim} relational model to map
     * @return an instantiated, client-facing {@link ClaimResponse} transfer model
     */
    private ClaimResponse mapToResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .policyNumber(claim.getPolicyNumber())
                .claimantName(claim.getClaimantName())
                .claimType(claim.getClaimType())
                .incidentDate(claim.getIncidentDate())
                .claimAmount(claim.getClaimAmount())
                .status(claim.getStatus())
                .description(claim.getDescription())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}