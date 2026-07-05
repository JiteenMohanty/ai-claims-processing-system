package com.jiteen.claims.claim.application.service.impl;

import com.jiteen.claims.claim.api.exception.ClaimNotFoundException;
import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.application.event.ClaimEventPublisher;
import com.jiteen.claims.claim.application.mapper.ClaimMapper;
import com.jiteen.claims.claim.application.service.ClaimService;
import com.jiteen.claims.claim.config.CacheNames;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.entity.ClaimStatusHistory;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import com.jiteen.claims.claim.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.claim.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.claim.domain.event.ClaimRejectedEvent;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import com.jiteen.claims.claim.domain.repository.ClaimStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * This class coordinates the core operational business logic for processing and
 * transitioning insurance claims within the AI-Powered Insurance Claims Processing Platform.
 * It acts as the bridge between the inbound REST API request controller tier and the
 * persistent storage repository layer.
 * </p>
 * <p>
 * Following enterprise standards established in the reference Authentication
 * service, this class relies on declarative transaction demarcation, constructor-based
 * dependency injection via Lombok, and MapStruct-based DTO and entity transformations
 * to isolate infrastructure concerns.
 * </p>
 * <p>
 * As of Phase 5, this service additionally publishes Kafka domain events after each
 * significant claim lifecycle transition, enabling downstream microservices (AI Processing
 * Service, Notification Service) to react asynchronously without tight coupling.
 * Status transitions are recorded in the {@code claim_status_history} audit trail table.
 * </p>
 *
 * @author Jiteen
 * @version 2.0
 * @since Java 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private static final String SYSTEM_ACTOR = "SYSTEM";

    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
    private final ClaimEventPublisher claimEventPublisher;
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;

    /**
     * Ingests and initializes a new insurance claim within the system of record,
     * then publishes a {@link ClaimCreatedEvent} to trigger asynchronous AI analysis.
     *
     * <p>
     * Maps incoming request particulars to a clean domain model instance, forces
     * the initial workflow tracking status to {@link ClaimStatus#SUBMITTED}, persists
     * the entity, and publishes the creation event to the Kafka messaging infrastructure
     * for downstream consumption by the AI Processing Service.
     * </p>
     *
     * @param request the {@link CreateClaimRequest} payload containing consumer and
     *                policy contract vectors
     * @return a structured {@link ClaimResponse} representing the newly registered
     *         and persisted claim entity
     */
    @Override
    @CacheEvict(value = CacheNames.CLAIMS_LIST, allEntries = true)
    public ClaimResponse createClaim(CreateClaimRequest request) {
        Claim claim = claimMapper.toEntity(request);
        Claim savedClaim = claimRepository.save(claim);

        log.info("Claim created with id: {} and status: {}", savedClaim.getId(), savedClaim.getStatus());

        ClaimCreatedEvent event = ClaimCreatedEvent.builder()
                .claimId(savedClaim.getId())
                .policyNumber(savedClaim.getPolicyNumber())
                .claimType(savedClaim.getClaimType())
                .claimantName(savedClaim.getClaimantName())
                .submittedAt(savedClaim.getCreatedAt())
                .build();

        claimEventPublisher.publishClaimCreated(event);

        return claimMapper.toResponse(savedClaim);
    }

    /**
     * Locates and retrieves an active, non-deleted insurance claim matching the
     * provided unique reference key.
     *
     * @param claimId the unique {@link UUID} key identifying the requested claim record
     * @return a fully populated {@link ClaimResponse} mapping the active entity data context
     * @throws ClaimNotFoundException if no matching active claim is found within the data
     *                                persistence layer
     */
    @Override
    @Cacheable(value = CacheNames.CLAIMS, key = "#claimId")
    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
        return claimMapper.toResponse(claim);
    }

    /**
     * Compiles and maps a listing of all active, non-deleted insurance claims
     * stored in the database registry.
     *
     * @return a {@link List} of {@link ClaimResponse} objects representing all
     *         matching active claims; returns an empty list if no records exist
     */
    @Override
    @Cacheable(value = CacheNames.CLAIMS_LIST, key = "'all'")
    @Transactional(readOnly = true)
    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findByDeletedAtIsNull()
                .stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    /**
     * Authorizes and approves an active insurance claim, advancing its lifecycle to
     * the APPROVED terminal phase, recording the transition in the audit trail, and
     * publishing a {@link ClaimApprovedEvent} to notify the Notification Service.
     *
     * @param claimId the unique {@link UUID} key tracking the target insurance claim entry
     * @return an updated {@link ClaimResponse} showing the successful shift to the approved state
     * @throws ClaimNotFoundException if the target claim cannot be found or has been soft-deleted
     */
    @Override
    @Caching(
        put  = @CachePut(value = CacheNames.CLAIMS, key = "#claimId"),
        evict = @CacheEvict(value = CacheNames.CLAIMS_LIST, allEntries = true)
    )
    public ClaimResponse approveClaim(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        ClaimStatus previousStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.APPROVED);
        Claim updatedClaim = claimRepository.save(claim);

        recordStatusTransition(claimId, previousStatus, ClaimStatus.APPROVED, SYSTEM_ACTOR);

        claimEventPublisher.publishClaimApproved(ClaimApprovedEvent.builder()
                .claimId(claimId)
                .policyNumber(claim.getPolicyNumber())
                .claimantName(claim.getClaimantName())
                .approvedBy(SYSTEM_ACTOR)
                .approvedAt(LocalDateTime.now())
                .build());

        log.info("Claim approved: claimId={}", claimId);

        return claimMapper.toResponse(updatedClaim);
    }

    /**
     * Formally denies and rejects an active insurance claim, locking it into a terminal
     * rejection state, recording the transition in the audit trail, and publishing a
     * {@link ClaimRejectedEvent} to notify the Notification Service.
     *
     * @param claimId the unique {@link UUID} key tracking the target insurance claim entry
     * @return an updated {@link ClaimResponse} showing the permanent shift to the rejected state
     * @throws ClaimNotFoundException if the target claim cannot be found or has been soft-deleted
     */
    @Override
    @Caching(
        put  = @CachePut(value = CacheNames.CLAIMS, key = "#claimId"),
        evict = @CacheEvict(value = CacheNames.CLAIMS_LIST, allEntries = true)
    )
    public ClaimResponse rejectClaim(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        ClaimStatus previousStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.REJECTED);
        Claim updatedClaim = claimRepository.save(claim);

        recordStatusTransition(claimId, previousStatus, ClaimStatus.REJECTED, SYSTEM_ACTOR);

        claimEventPublisher.publishClaimRejected(ClaimRejectedEvent.builder()
                .claimId(claimId)
                .policyNumber(claim.getPolicyNumber())
                .claimantName(claim.getClaimantName())
                .rejectedBy(SYSTEM_ACTOR)
                .reason("Claim rejected by claim officer")
                .rejectedAt(LocalDateTime.now())
                .build());

        log.info("Claim rejected: claimId={}", claimId);

        return claimMapper.toResponse(updatedClaim);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put  = @CachePut(value = CacheNames.CLAIMS, key = "#claimId"),
        evict = @CacheEvict(value = CacheNames.CLAIMS_LIST, allEntries = true)
    )
    public ClaimResponse updateClaim(UUID claimId, UpdateClaimRequest request) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
        claimMapper.updateClaimFromRequest(request, claim);
        Claim updatedClaim = claimRepository.save(claim);
        return claimMapper.toResponse(updatedClaim);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.CLAIMS, key = "#claimId"),
        @CacheEvict(value = CacheNames.CLAIMS_LIST, allEntries = true)
    })
    public void deleteClaim(UUID claimId) {
        Claim claim = claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
        claim.setDeletedAt(LocalDateTime.now());
        claimRepository.save(claim);
    }

    /**
     * Appends an immutable status transition record to the claim lifecycle audit trail.
     *
     * @param claimId    the claim whose status changed
     * @param oldStatus  the lifecycle status before the transition
     * @param newStatus  the lifecycle status after the transition
     * @param changedBy  the actor identifier that triggered the transition
     */
    private void recordStatusTransition(UUID claimId, ClaimStatus oldStatus,
                                        ClaimStatus newStatus, String changedBy) {
        ClaimStatusHistory history = ClaimStatusHistory.builder()
                .claimId(claimId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        claimStatusHistoryRepository.save(history);
    }
}
