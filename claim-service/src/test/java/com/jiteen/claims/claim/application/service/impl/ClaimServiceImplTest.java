package com.jiteen.claims.claim.application.service.impl;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.application.event.ClaimEventPublisher;
import com.jiteen.claims.claim.application.mapper.ClaimMapper;
import com.jiteen.claims.claim.api.exception.ClaimNotFoundException;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.entity.ClaimStatusHistory;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import com.jiteen.claims.claim.domain.event.ClaimApprovedEvent;
import com.jiteen.claims.claim.domain.event.ClaimCreatedEvent;
import com.jiteen.claims.claim.domain.event.ClaimRejectedEvent;
import com.jiteen.claims.claim.domain.repository.ClaimRepository;
import com.jiteen.claims.claim.domain.repository.ClaimStatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test suite for verifying the core business logic implementations
 * inside {@link ClaimServiceImpl}.
 *
 * <p>
 * This suite leverages JUnit 5, AssertJ, and Mockito with the
 * {@link MockitoExtension} to establish high-performance, container-less isolation.
 * It eliminates infrastructure layer components such as databases or Spring application
 * contexts, focusing entirely on state transformations, contract compliance,
 * exceptional paths, repository interaction behaviors, and Kafka event publishing
 * verification.
 * </p>
 *
 * @author Jiteen
 * @version 2.0
 * @since Java 21
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimServiceImpl Business Logic Unit Tests")
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private ClaimEventPublisher claimEventPublisher;

    @Mock
    private ClaimStatusHistoryRepository claimStatusHistoryRepository;

    @InjectMocks
    private ClaimServiceImpl claimService;

    @Test
    @DisplayName("Should create claim successfully, transition status, persist record, and publish ClaimCreatedEvent")
    void shouldCreateClaimSuccessfully() {
        // Given
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyNumber("POL-444555")
                .claimantName("John Connor")
                .claimType("AUTO")
                .incidentDate(LocalDateTime.now().minusDays(1))
                .claimAmount(new BigDecimal("3500.00"))
                .description("Front bumper displacement asset recovery scenario.")
                .build();

        Claim unpersistedEntity = Claim.builder()
                .policyNumber(request.getPolicyNumber())
                .claimantName(request.getClaimantName())
                .claimType(request.getClaimType())
                .incidentDate(request.getIncidentDate())
                .claimAmount(request.getClaimAmount())
                .description(request.getDescription())
                .status(ClaimStatus.SUBMITTED)
                .build();

        Claim savedEntity = Claim.builder()
                .id(UUID.randomUUID())
                .policyNumber(unpersistedEntity.getPolicyNumber())
                .claimantName(unpersistedEntity.getClaimantName())
                .claimType(unpersistedEntity.getClaimType())
                .incidentDate(unpersistedEntity.getIncidentDate())
                .claimAmount(unpersistedEntity.getClaimAmount())
                .description(unpersistedEntity.getDescription())
                .status(ClaimStatus.SUBMITTED)
                .build();

        ClaimResponse expectedResponse = ClaimResponse.builder()
                .id(savedEntity.getId())
                .policyNumber(savedEntity.getPolicyNumber())
                .claimantName(savedEntity.getClaimantName())
                .claimType(savedEntity.getClaimType())
                .incidentDate(savedEntity.getIncidentDate())
                .claimAmount(savedEntity.getClaimAmount())
                .status(ClaimStatus.SUBMITTED)
                .description(savedEntity.getDescription())
                .createdAt(savedEntity.getCreatedAt())
                .updatedAt(savedEntity.getUpdatedAt())
                .build();

        when(claimMapper.toEntity(request)).thenReturn(unpersistedEntity);
        when(claimRepository.save(unpersistedEntity)).thenReturn(savedEntity);
        when(claimMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        // When
        ClaimResponse actualResponse = claimService.createClaim(request);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(savedEntity.getId());
        assertThat(actualResponse.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(actualResponse.getPolicyNumber()).isEqualTo("POL-444555");

        verify(claimMapper).toEntity(request);
        verify(claimRepository).save(unpersistedEntity);
        verify(claimMapper).toResponse(savedEntity);
        verify(claimEventPublisher).publishClaimCreated(any(ClaimCreatedEvent.class));
    }

    @Test
    @DisplayName("Should successfully retrieve an active claim by ID when it exists and is not soft-deleted")
    void shouldGetClaimByIdSuccessfully() {
        // Given
        UUID claimId = UUID.randomUUID();
        Claim existingClaim = Claim.builder()
                .id(claimId)
                .policyNumber("POL-777")
                .claimantName("Marcus Wright")
                .claimType("PROPERTY")
                .status(ClaimStatus.UNDER_REVIEW)
                .build();

        ClaimResponse expectedResponse = ClaimResponse.builder()
                .id(claimId)
                .policyNumber("POL-777")
                .claimantName("Marcus Wright")
                .claimType("PROPERTY")
                .status(ClaimStatus.UNDER_REVIEW)
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));
        when(claimMapper.toResponse(existingClaim)).thenReturn(expectedResponse);

        // When
        ClaimResponse actualResponse = claimService.getClaimById(claimId);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(claimId);
        assertThat(actualResponse.getClaimantName()).isEqualTo("Marcus Wright");

        verify(claimRepository).findByIdAndDeletedAtIsNull(claimId);
        verify(claimMapper).toResponse(existingClaim);
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when looking up a non-existent or soft-deleted identifier")
    void shouldThrowExceptionWhenClaimNotFound() {
        // Given
        UUID nonexistentId = UUID.randomUUID();
        when(claimRepository.findByIdAndDeletedAtIsNull(nonexistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> claimService.getClaimById(nonexistentId))
                .isInstanceOf(ClaimNotFoundException.class)
                .hasMessageContaining("Claim not found with id: " + nonexistentId);

        verify(claimRepository).findByIdAndDeletedAtIsNull(nonexistentId);
    }

    @Test
    @DisplayName("Should return all active claims")
    void shouldGetAllClaimsSuccessfully() {
        // Given
        Claim activeClaim1 = Claim.builder().id(UUID.randomUUID()).policyNumber("POL-1").build();
        Claim activeClaim2 = Claim.builder().id(UUID.randomUUID()).policyNumber("POL-2").build();

        ClaimResponse response1 = ClaimResponse.builder().id(activeClaim1.getId()).policyNumber("POL-1").build();
        ClaimResponse response2 = ClaimResponse.builder().id(activeClaim2.getId()).policyNumber("POL-2").build();

        when(claimRepository.findByDeletedAtIsNull()).thenReturn(List.of(activeClaim1, activeClaim2));
        when(claimMapper.toResponse(activeClaim1)).thenReturn(response1);
        when(claimMapper.toResponse(activeClaim2)).thenReturn(response2);

        // When
        List<ClaimResponse> resultList = claimService.getAllClaims();

        // Then
        assertThat(resultList).isNotNull().hasSize(2);
        assertThat(resultList.get(0).getPolicyNumber()).isEqualTo("POL-1");
        assertThat(resultList.get(1).getPolicyNumber()).isEqualTo("POL-2");

        verify(claimRepository).findByDeletedAtIsNull();
        verify(claimMapper).toResponse(activeClaim1);
        verify(claimMapper).toResponse(activeClaim2);
    }

    @Test
    @DisplayName("Should update status to APPROVED, record history, persist changes, and publish ClaimApprovedEvent")
    void shouldApproveClaimSuccessfully() {
        // Given
        UUID claimId = UUID.randomUUID();
        Claim existingClaim = Claim.builder()
                .id(claimId)
                .policyNumber("POL-APPROVE")
                .claimantName("Sarah Connor")
                .status(ClaimStatus.AI_REVIEW_COMPLETED)
                .build();

        Claim approvedClaim = Claim.builder()
                .id(claimId)
                .status(ClaimStatus.APPROVED)
                .build();

        ClaimResponse expectedResponse = ClaimResponse.builder()
                .id(claimId)
                .status(ClaimStatus.APPROVED)
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));
        when(claimRepository.save(existingClaim)).thenReturn(approvedClaim);
        when(claimMapper.toResponse(approvedClaim)).thenReturn(expectedResponse);
        when(claimStatusHistoryRepository.save(any(ClaimStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ClaimResponse actualResponse = claimService.approveClaim(claimId);

        // Then
        assertThat(existingClaim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getStatus()).isEqualTo(ClaimStatus.APPROVED);

        verify(claimRepository).findByIdAndDeletedAtIsNull(claimId);
        verify(claimRepository).save(existingClaim);
        verify(claimMapper).toResponse(approvedClaim);

        ArgumentCaptor<ClaimStatusHistory> historyCaptor = ArgumentCaptor.forClass(ClaimStatusHistory.class);
        verify(claimStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOldStatus()).isEqualTo(ClaimStatus.AI_REVIEW_COMPLETED);
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(ClaimStatus.APPROVED);

        verify(claimEventPublisher).publishClaimApproved(any(ClaimApprovedEvent.class));
    }

    @Test
    @DisplayName("Should update status to REJECTED, record history, persist changes, and publish ClaimRejectedEvent")
    void shouldRejectClaimSuccessfully() {
        // Given
        UUID claimId = UUID.randomUUID();
        Claim existingClaim = Claim.builder()
                .id(claimId)
                .policyNumber("POL-REJECT")
                .claimantName("John Doe")
                .status(ClaimStatus.AI_REVIEW_COMPLETED)
                .build();

        Claim rejectedClaim = Claim.builder()
                .id(claimId)
                .status(ClaimStatus.REJECTED)
                .build();

        ClaimResponse expectedResponse = ClaimResponse.builder()
                .id(claimId)
                .status(ClaimStatus.REJECTED)
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));
        when(claimRepository.save(existingClaim)).thenReturn(rejectedClaim);
        when(claimMapper.toResponse(rejectedClaim)).thenReturn(expectedResponse);
        when(claimStatusHistoryRepository.save(any(ClaimStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ClaimResponse actualResponse = claimService.rejectClaim(claimId);

        // Then
        assertThat(existingClaim.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getStatus()).isEqualTo(ClaimStatus.REJECTED);

        verify(claimRepository).findByIdAndDeletedAtIsNull(claimId);
        verify(claimRepository).save(existingClaim);
        verify(claimMapper).toResponse(rejectedClaim);

        ArgumentCaptor<ClaimStatusHistory> historyCaptor = ArgumentCaptor.forClass(ClaimStatusHistory.class);
        verify(claimStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOldStatus()).isEqualTo(ClaimStatus.AI_REVIEW_COMPLETED);
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(ClaimStatus.REJECTED);

        verify(claimEventPublisher).publishClaimRejected(any(ClaimRejectedEvent.class));
    }

    @Test
    @DisplayName("Should perform partial update mapping fields cleanly onto target entity resource and save changes")
    void shouldUpdateClaimSuccessfully() {
        // Given
        UUID claimId = UUID.randomUUID();
        UpdateClaimRequest updateRequest = UpdateClaimRequest.builder()
                .claimantName("Sarah Connor")
                .description("Revised cybernetic risk estimation update profile.")
                .build();

        Claim existingClaim = Claim.builder()
                .id(claimId)
                .policyNumber("POL-101")
                .claimantName("John Doe")
                .description("Old context")
                .status(ClaimStatus.SUBMITTED)
                .build();

        ClaimResponse expectedResponse = ClaimResponse.builder()
                .id(claimId)
                .policyNumber("POL-101")
                .claimantName("Sarah Connor")
                .description("Revised cybernetic risk estimation update profile.")
                .status(ClaimStatus.SUBMITTED)
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));

        doAnswer(invocation -> {
            Claim target = invocation.getArgument(1);
            target.setClaimantName(updateRequest.getClaimantName());
            target.setDescription(updateRequest.getDescription());
            return null;
        }).when(claimMapper).updateClaimFromRequest(updateRequest, existingClaim);

        when(claimRepository.save(existingClaim)).thenReturn(existingClaim);
        when(claimMapper.toResponse(existingClaim)).thenReturn(expectedResponse);

        // When
        ClaimResponse actualResponse = claimService.updateClaim(claimId, updateRequest);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getClaimantName()).isEqualTo("Sarah Connor");
        assertThat(actualResponse.getPolicyNumber()).isEqualTo("POL-101");

        verify(claimRepository).findByIdAndDeletedAtIsNull(claimId);
        verify(claimMapper).updateClaimFromRequest(updateRequest, existingClaim);
        verify(claimRepository).save(existingClaim);
        verify(claimMapper).toResponse(existingClaim);
    }

    @Test
    @DisplayName("Should flag entity with a populated deletion timestamp value to fulfill soft delete constraint semantics")
    void shouldDeleteClaimSuccessfully() {
        // Given
        UUID claimId = UUID.randomUUID();
        Claim existingClaim = Claim.builder()
                .id(claimId)
                .policyNumber("POL-999")
                .build();

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(existingClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        claimService.deleteClaim(claimId);

        // Then
        verify(claimRepository).findByIdAndDeletedAtIsNull(claimId);
        verify(claimRepository).save(existingClaim);

        assertThat(existingClaim.getDeletedAt()).isNotNull();
        assertThat(existingClaim.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
