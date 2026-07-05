package com.jiteen.claims.claim.application.service.impl;

import com.jiteen.claims.claim.api.exception.AiAnalysisNotFoundException;
import com.jiteen.claims.claim.api.exception.ClaimNotFoundException;
import com.jiteen.claims.claim.application.dto.response.AiAnalysisResultResponse;
import com.jiteen.claims.claim.application.mapper.AiAnalysisResultMapper;
import com.jiteen.claims.claim.application.service.AiAnalysisResultService;
import com.jiteen.claims.claim.domain.entity.AiAnalysisResult;
import com.jiteen.claims.claim.domain.repository.AiAnalysisResultRepository;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service implementation providing read access to AI analysis results
 * associated with insurance claims within the AI-Powered Insurance Claims Processing Platform.
 *
 * <p>
 * This service validates parent claim existence before delegating result retrieval
 * to the repository layer, ensuring meaningful error responses when a claim is absent
 * versus when analysis is still pending. All operations are scoped as read-only
 * transactions to optimize database performance.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiAnalysisResultServiceImpl implements AiAnalysisResultService {

    private final ClaimRepository claimRepository;
    private final AiAnalysisResultRepository aiAnalysisResultRepository;
    private final AiAnalysisResultMapper aiAnalysisResultMapper;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Validates that the parent claim exists and is not soft-deleted before
     * attempting to locate the associated AI analysis result. Raises distinct
     * exceptions for missing claims versus pending AI analysis to allow clients
     * to distinguish between the two states.
     * </p>
     */
    @Override
    public AiAnalysisResultResponse getAnalysisResultByClaimId(UUID claimId) {
        log.debug("Retrieving AI analysis result for claimId: {}", claimId);

        claimRepository.findByIdAndDeletedAtIsNull(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        AiAnalysisResult result = aiAnalysisResultRepository.findByClaimId(claimId)
                .orElseThrow(() -> new AiAnalysisNotFoundException(claimId));

        return aiAnalysisResultMapper.toResponse(result);
    }
}
