package com.jiteen.claims.claim.application.service;

import com.jiteen.claims.claim.application.dto.response.AiAnalysisResultResponse;

import java.util.UUID;

/**
 * Application service contract defining read operations for AI analysis results
 * associated with insurance claims within the AI-Powered Insurance Claims
 * Processing Platform.
 *
 * <p>
 * AI analysis results are written exclusively by the Kafka consumer layer upon
 * receiving {@code AI_ANALYSIS_COMPLETED} events from the AI Processing Service.
 * This service exposes query operations that allow claim officers and administrative
 * consumers to retrieve AI-generated intelligence via the REST API.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public interface AiAnalysisResultService {

    /**
     * Retrieves the AI analysis result record associated with the specified claim.
     *
     * @param claimId the unique {@link UUID} identifier of the parent insurance claim
     * @return a fully populated {@link AiAnalysisResultResponse} containing all
     *         AI-generated intelligence for the specified claim
     * @throws com.jiteen.claims.claim.api.exception.ClaimNotFoundException if the
     *         parent claim does not exist or has been soft-deleted
     * @throws com.jiteen.claims.claim.api.exception.AiAnalysisNotFoundException if
     *         no AI analysis result has been persisted for the specified claim yet
     */
    AiAnalysisResultResponse getAnalysisResultByClaimId(UUID claimId);
}
