package com.jiteen.claims.claim.api.controller;

import com.jiteen.claims.claim.application.dto.response.AiAnalysisResultResponse;
import com.jiteen.claims.claim.application.service.AiAnalysisResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST API Controller exposing read-only endpoint contracts for retrieving
 * AI-generated analysis results associated with insurance claims.
 *
 * <p>
 * This controller operates as a pure presentation layer component, delegating
 * all data retrieval operations to the {@link AiAnalysisResultService} application
 * service. It provides claim officers and administrative consumers with structured
 * access to AI intelligence outputs including risk scores, fraud indicators,
 * OCR-extracted metadata, and processing recommendations.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@RestController
@RequestMapping("/api/v1/ai-analysis")
@RequiredArgsConstructor
@Tag(name = "AI Analysis Results", description = "REST API interfaces for retrieving AI-generated analysis intelligence associated with insurance claim records.")
public class AiAnalysisResultController {

    private final AiAnalysisResultService aiAnalysisResultService;

    /**
     * Retrieves the AI-generated analysis result for the specified insurance claim.
     *
     * <p>
     * Returns a 404 response if the parent claim does not exist, or if the
     * AI Processing Service has not yet completed analysis for this claim.
     * Clients should poll this endpoint after claim submission or check the
     * claim status field for the {@code AI_REVIEW_COMPLETED} lifecycle indicator.
     * </p>
     *
     * @param claimId the unique {@link UUID} tracking token of the parent insurance claim
     * @return a {@link ResponseEntity} wrapping the {@link AiAnalysisResultResponse}
     *         payload along with an HTTP 200 OK status
     */
    @GetMapping("/{claimId}")
    @Operation(
        summary = "Retrieve AI analysis results for a specific claim",
        description = "Fetches the complete AI-generated intelligence payload for a specified claim, " +
                      "including risk score, fraud indicators, OCR-extracted metadata, and recommendation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "AI analysis result successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Parent claim not found or AI analysis not yet completed"),
        @ApiResponse(responseCode = "500", description = "Internal error occurred during AI result retrieval")
    })
    public ResponseEntity<AiAnalysisResultResponse> getAiAnalysisResult(
            @PathVariable UUID claimId) {

        AiAnalysisResultResponse response = aiAnalysisResultService.getAnalysisResultByClaimId(claimId);
        return ResponseEntity.ok(response);
    }
}
