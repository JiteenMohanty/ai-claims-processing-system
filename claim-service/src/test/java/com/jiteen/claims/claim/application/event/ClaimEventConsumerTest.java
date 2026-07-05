package com.jiteen.claims.claim.application.event;

import com.jiteen.claims.claim.domain.entity.AiAnalysisResult;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.entity.ClaimStatusHistory;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import com.jiteen.claims.claim.domain.event.AiAnalysisCompletedEvent;
import com.jiteen.claims.claim.domain.repository.AiAnalysisResultRepository;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import com.jiteen.claims.claim.domain.repository.ClaimStatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test suite for {@link ClaimEventConsumer} verifying the
 * AI analysis result persistence and claim lifecycle advancement logic.
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimEventConsumer Unit Tests")
class ClaimEventConsumerTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private AiAnalysisResultRepository aiAnalysisResultRepository;

    @Mock
    private ClaimStatusHistoryRepository claimStatusHistoryRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private ClaimEventConsumer claimEventConsumer;

    @Test
    @DisplayName("Should advance claim status to AI_REVIEW_COMPLETED and persist analysis result on valid event")
    void shouldProcessAiAnalysisCompletedEventSuccessfully() {
        // Given
        UUID claimId = UUID.randomUUID();

        Claim existingClaim = Claim.builder()
                .id(claimId)
                .policyNumber("POL-AI-001")
                .claimantName("John Doe")
                .status(ClaimStatus.SUBMITTED)
                .build();

        AiAnalysisCompletedEvent event = AiAnalysisCompletedEvent.builder()
                .claimId(claimId)
                .riskScore(55)
                .summary("AI analysis complete. Medium risk claim.")
                .fraudIndicators(List.of())
                .recommendedAction("MANUAL_REVIEW_REQUIRED")
                .policyNumberExtracted("POL-AI-001")
                .customerNameExtracted("John Doe")
                .missingDocuments(List.of("Police report"))
                .processedAt(LocalDateTime.now())
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(claimStatusHistoryRepository.save(any(ClaimStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(aiAnalysisResultRepository.existsByClaimId(claimId)).thenReturn(false);
        when(aiAnalysisResultRepository.save(any(AiAnalysisResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cacheManager.getCache(any())).thenReturn(cache);

        // When
        claimEventConsumer.handleAiAnalysisCompleted(event);

        // Then
        assertThat(existingClaim.getStatus()).isEqualTo(ClaimStatus.AI_REVIEW_COMPLETED);

        verify(claimRepository).save(existingClaim);

        ArgumentCaptor<ClaimStatusHistory> historyCaptor = ArgumentCaptor.forClass(ClaimStatusHistory.class);
        verify(claimStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOldStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(ClaimStatus.AI_REVIEW_COMPLETED);

        ArgumentCaptor<AiAnalysisResult> resultCaptor = ArgumentCaptor.forClass(AiAnalysisResult.class);
        verify(aiAnalysisResultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getClaimId()).isEqualTo(claimId);
        assertThat(resultCaptor.getValue().getRiskScore()).isEqualTo(55);
        assertThat(resultCaptor.getValue().getRecommendedAction()).isEqualTo("MANUAL_REVIEW_REQUIRED");
    }

    @Test
    @DisplayName("Should skip analysis result persistence if result already exists for the claim")
    void shouldSkipDuplicateAnalysisResultPersistence() {
        // Given
        UUID claimId = UUID.randomUUID();

        Claim existingClaim = Claim.builder()
                .id(claimId)
                .status(ClaimStatus.AI_REVIEW_IN_PROGRESS)
                .build();

        AiAnalysisCompletedEvent event = AiAnalysisCompletedEvent.builder()
                .claimId(claimId)
                .riskScore(40)
                .summary("Analysis done.")
                .fraudIndicators(List.of())
                .recommendedAction("APPROVE")
                .processedAt(LocalDateTime.now())
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(claimStatusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aiAnalysisResultRepository.existsByClaimId(claimId)).thenReturn(true);
        when(cacheManager.getCache(any())).thenReturn(cache);

        // When
        claimEventConsumer.handleAiAnalysisCompleted(event);

        // Then
        verify(aiAnalysisResultRepository, never()).save(any(AiAnalysisResult.class));
        assertThat(existingClaim.getStatus()).isEqualTo(ClaimStatus.AI_REVIEW_COMPLETED);
    }

    @Test
    @DisplayName("Should silently ignore events for non-existent or soft-deleted claims")
    void shouldIgnoreEventForNonExistentClaim() {
        // Given
        UUID unknownClaimId = UUID.randomUUID();

        AiAnalysisCompletedEvent event = AiAnalysisCompletedEvent.builder()
                .claimId(unknownClaimId)
                .riskScore(70)
                .processedAt(LocalDateTime.now())
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(unknownClaimId)).thenReturn(Optional.empty());

        // When
        claimEventConsumer.handleAiAnalysisCompleted(event);

        // Then — no repository writes should occur
        verify(claimRepository, never()).save(any());
        verify(claimStatusHistoryRepository, never()).save(any());
        verify(aiAnalysisResultRepository, never()).save(any());
    }
}
