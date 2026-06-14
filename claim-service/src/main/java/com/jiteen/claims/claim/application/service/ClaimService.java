package com.jiteen.claims.claim.application.service;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service layer interface defining the business contract for managing insurance claims
 * within the AI-Powered Insurance Claims Processing Platform.
 * <p>
 * This interface encapsulates all primary lifecycle operations and workflow state transitions
 * for claims, abstracting the underlying domain logic, persistence layer mechanics, and 
 * transactional boundaries from the REST API exposure controllers[cite: 60]. All interactions are handled 
 * through dedicated Data Transfer Objects (DTOs) to ensure strict separation of concerns.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface ClaimService {

    /**
     * Ingests and initializes a new insurance claim within the processing pipeline.
     * <p>
     * This operation performs early validation against incoming request criteria, maps payload 
     * structural constraints, initializes audit tracking fields, and assigns the initial 
     * state machine status before persisting the claim entity into the system of record.
     * </p>
     *
     * @param request the {@link CreateClaimRequest} payload containing validated claimant 
     * and policy particulars
     * @return a {@link ClaimResponse} summarizing the instantiated claim state, unique identifier, 
     * and initial status
     */
    ClaimResponse createClaim(CreateClaimRequest request);

    /**
     * Retrieves an active, non-deleted claim record uniquely identified by its database primary key.
     *
     * @param claimId the unique {@link UUID} tracking the targeted insurance claim
     * @return the corresponding {@link ClaimResponse} delivery payload mapping the retrieved domain record
     */
    ClaimResponse getClaimById(UUID claimId);

    /**
     * Compiles an un-paginated collection of all active, non-deleted claims stored across 
     * the enterprise domain.
     * <p>
     * Primarily utilized for administrative processing pipelines, tracking metrics dashboards, 
     * or historical audit monitoring indices.
     * </p>
     *
     * @return a {@link List} containing {@link ClaimResponse} payloads for all matching active claims; 
     * an empty list if no active records exist
     */
    List<ClaimResponse> getAllClaims();

    /**
     * Transitions an active claim's lifecycle state machine to an authorized confirmation phase.
     * <p>
     * This operation signifies successful passing of all rule-based conditions, AI analysis validation, 
     * or dedicated manual verification by an operational Claim Officer, setting up the resource for 
     * financial payout fulfillment.
     * </p>
     *
     * @param claimId the unique {@link UUID} tracking the targeted insurance claim
     * @return the updated {@link ClaimResponse} illustrating the transitioned status matrix
     */
    ClaimResponse approveClaim(UUID claimId);

    /**
     * Transitions an active claim's lifecycle state machine to a terminal denial phase.
     * <p>
     * Executed when policy mismatch parameters, high-risk automated fraud indices, or explicit manual 
     * determinations verify that the claim submission does not comply with insurance coverage rules.
     * </p>
     *
     * @param claimId the unique {@link UUID} tracking the targeted insurance claim
     * @return the updated {@link ClaimResponse} illustrating the terminal rejected status
     */
    ClaimResponse rejectClaim(UUID claimId);
}