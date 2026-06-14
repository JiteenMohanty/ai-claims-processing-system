package com.jiteen.claims.claim.api.controller;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.application.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST API Controller exposes endpoint contracts for coordinating insurance
 * claim resources.
 * <p>
 * This component handles incoming HTTP traffic routing, triggers request body
 * payload data validation,
 * maps operational exceptions, and returns standard enterprise REST status
 * structures. It operates
 * strictly as a lightweight presentation layer controller, maintaining zero
 * business logic rules
 * and delegating execution contexts directly into the application service
 * infrastructure layer.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Validated
@Tag(name = "Claim Management", description = "REST API interfaces managing insurance claim ingestion pipelines and workflow lifecycle transactions.")
public class ClaimController {

    private final ClaimService claimService;

    /**
     * Ingests a new insurance claim transaction payload into the platform
     * processing pipeline.
     * <p>
     * Validates the structural constraints of the incoming payload, assigns
     * tracking metrics via the application layer,
     * routes initial states, and registers the newly created record inside the
     * transactional system of record.
     * </p>
     *
     * @param request the {@link CreateClaimRequest} encapsulating data integrity
     *                rules and claim attributes
     * @return a {@link ResponseEntity} containing the initialized
     *         {@link ClaimResponse} payload along with an HTTP 201 Created header
     *         status
     */
    @PostMapping
    @Operation(summary = "Create and ingest a new insurance claim", description = "Processes an incoming claim proposal payload, triggers structural schema constraints validation, and sets the state machine tracker to SUBMITTED status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Insurance claim resource successfully instantiated and persisted"),
            @ApiResponse(responseCode = "400", description = "Provided payload failed constraint matching or missing data requirements"),
            @ApiResponse(responseCode = "500", description = "Internal infrastructure error occurred while compiling the transactional claim resource")
    })
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateClaimRequest request) {
        ClaimResponse claimResponse = claimService.createClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(claimResponse);
    }

    /**
     * Fetches details of an active, non-deleted insurance claim matching the
     * provided unique identifier.
     *
     * @param claimId the unique {@link UUID} tracking token mapping a claim record
     *                in the database
     * @return a {@link ResponseEntity} wrapping the mapped {@link ClaimResponse}
     *         metadata block along with an HTTP 200 OK header status
     */
    @GetMapping("/{claimId}")
    @Operation(summary = "Retrieve an active claim by unique identifier", description = "Locates and extracts structural transaction information for an active, non-deleted claim mapped to the specific UUID reference.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active insurance claim record retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Target claim resource not found or matching record has been soft-deleted"),
            @ApiResponse(responseCode = "500", description = "Internal system exception occurred while locating the requested object reference")
    })
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable("claimId") UUID claimId) {
        ClaimResponse claimResponse = claimService.getClaimById(claimId);
        return ResponseEntity.ok(claimResponse);
    }

    /**
     * Compiles an array containing all active, un-deleted insurance claims managed
     * across the platform context.
     *
     * @return a {@link ResponseEntity} wrapping a {@link List} of
     *         {@link ClaimResponse} records along with an HTTP 200 OK header status
     */
    @GetMapping
    @Operation(summary = "Retrieve a listing of all active claims", description = "Compiles an array tracking all non-deleted claim records currently registered across active infrastructure layers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprehensive listing of active claims compiled successfully"),
            @ApiResponse(responseCode = "500", description = "Internal processing boundary error occurred during extraction optimization operations")
    })
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        List<ClaimResponse> claims = claimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }

    /**
     * Evaluates and updates an active insurance claim transition marker directly to
     * the APPROVED state machine phase.
     * <p>
     * Signifies successful authorization checkpoint confirmation, indicating that
     * the targeted claim entity complies
     * with all insurance business policies and rules.
     * </p>
     *
     * @param claimId the unique {@link UUID} tracking token mapping a claim record
     *                in the database
     * @return a {@link ResponseEntity} holding the updated state
     *         {@link ClaimResponse} layout along with an HTTP 200 OK header status
     */
    @PutMapping("/{claimId}/approve")
    @Operation(summary = "Approve an active insurance claim transaction", description = "Advances an active claim's internal workflow status variable explicitly to the APPROVED state vector matrix.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Target claim successfully advanced to authorized compliance phase"),
            @ApiResponse(responseCode = "400", description = "Requested modification context violates core state machine transition rules"),
            @ApiResponse(responseCode = "404", description = "Target claim resource not found for the provided identification token"),
            @ApiResponse(responseCode = "500", description = "Internal framework transaction error occurred during status advancement execution")
    })
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable UUID claimId) {
        ClaimResponse claimResponse = claimService.approveClaim(claimId);
        return ResponseEntity.ok(claimResponse);
    }

    /**
     * Evaluates and shifts an active insurance claim transition status directly to
     * the terminal REJECTED state machine phase.
     * <p>
     * Signifies definitive administrative or policy-based refusal, marking the
     * claim as dead or archived for subsequent audit investigations.
     * </p>
     *
     * @param claimId the unique {@link UUID} tracking token mapping a claim record
     *                in the database
     * @return a {@link ResponseEntity} holding the updated state
     *         {@link ClaimResponse} layout along with an HTTP 200 OK header status
     */
    @PutMapping("/{claimId}/reject")
    @Operation(summary = "Formally deny and reject an active insurance claim transaction", description = "Advances an active claim's workflow track status variable into a terminal REJECTED denial loop phase.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Target claim successfully moved into a terminal rejection state loop"),
            @ApiResponse(responseCode = "400", description = "Requested modification context violates core state machine transition rules"),
            @ApiResponse(responseCode = "404", description = "Target claim resource not found for the provided identification token"),
            @ApiResponse(responseCode = "500", description = "Internal framework transaction error occurred during state adjustment mapping execution")
    })
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable UUID claimId) {
        ClaimResponse claimResponse = claimService.rejectClaim(claimId);
        return ResponseEntity.ok(claimResponse);
    }

    @PutMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> updateClaim(
            @PathVariable UUID claimId,
            @Valid @RequestBody UpdateClaimRequest request) {

        ClaimResponse response = claimService.updateClaim(claimId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{claimId}")
    public ResponseEntity<Void> deleteClaim(
            @PathVariable UUID claimId) {

        claimService.deleteClaim(claimId);

        return ResponseEntity.noContent().build();
    }
}