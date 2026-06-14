package com.jiteen.claims.claim.application.mapper;

import com.jiteen.claims.claim.application.dto.request.CreateClaimRequest;
import com.jiteen.claims.claim.application.dto.request.UpdateClaimRequest;
import com.jiteen.claims.claim.application.dto.response.ClaimResponse;
import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused lightweight unit test suite for verifying the structural mapping transformations
 * defined in {@link ClaimMapper}.
 * <p>
 * This class executes isolated programmatic verification of MapStruct-generated translation
 * behavior without bootstrapping a heavy Spring Boot container context or utilizing mocks, ensuring
 * optimal runtime performance and reliable test-driven boundary confirmation.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@DisplayName("ClaimMapper Compilation and Mapping Integrity Tests")
class ClaimMapperTest {

    private final ClaimMapper mapper = Mappers.getMapper(ClaimMapper.class);

    /**
     * Assers that an incoming {@link CreateClaimRequest} translates correctly to an un-persisted
     * {@link Claim} entity template with the status forced to {@link ClaimStatus#SUBMITTED}.
     */
    @Test
    @DisplayName("Should cleanly map valid CreateClaimRequest fields to Claim entity and enforce initial status")
    void shouldMapCreateClaimRequestToEntity() {
        // Given
        LocalDateTime incidentTime = LocalDateTime.of(2026, 6, 1, 10, 30);
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyNumber("POL-777888")
                .claimantName("Jane Alexandra Doe")
                .claimType("PROPERTY")
                .incidentDate(incidentTime)
                .claimAmount(new BigDecimal("12500.50"))
                .description("Comprehensive structural baseline building foundation crack evaluation.")
                .build();

        // When
        Claim entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getPolicyNumber()).isEqualTo("POL-777888");
        assertThat(entity.getClaimantName()).isEqualTo("Jane Alexandra Doe");
        assertThat(entity.getClaimType()).isEqualTo("PROPERTY");
        assertThat(entity.getIncidentDate()).isEqualTo(incidentTime);
        assertThat(entity.getClaimAmount()).isEqualByComparingTo("12500.50");
        assertThat(entity.getDescription()).isEqualTo("Comprehensive structural baseline building foundation crack evaluation.");
        assertThat(entity.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        assertThat(entity.getDeletedAt()).isNull();
    }

    /**
     * Asserts that a data-layer populated {@link Claim} entity completely copies its internal state matrix
     * to a public out-facing {@link ClaimResponse} DTO contract profile.
     */
    @Test
    @DisplayName("Should accurately replicate all persistent Claim entity columns onto an outbound ClaimResponse")
    void shouldMapEntityToResponse() {
        // Given
        UUID targetId = UUID.randomUUID();
        LocalDateTime incidentTime = LocalDateTime.of(2026, 5, 15, 14, 0);
        LocalDateTime creationTime = LocalDateTime.of(2026, 5, 16, 9, 0);
        LocalDateTime modificationTime = LocalDateTime.of(2026, 5, 20, 16, 45);

        Claim entity = Claim.builder()
                .id(targetId)
                .policyNumber("POL-111222")
                .claimantName("John Harrison Corp")
                .claimType("AUTO")
                .incidentDate(incidentTime)
                .claimAmount(new BigDecimal("4200.00"))
                .status(ClaimStatus.AI_REVIEW_COMPLETED)
                .description("Rear bumper minor collision damage via transport vehicle mismatch.")
                .build();

        entity.setCreatedAt(creationTime);
        entity.setUpdatedAt(modificationTime);

        // When
        ClaimResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(targetId);
        assertThat(response.getPolicyNumber()).isEqualTo("POL-111222");
        assertThat(response.getClaimantName()).isEqualTo("John Harrison Corp");
        assertThat(response.getClaimType()).isEqualTo("AUTO");
        assertThat(response.getIncidentDate()).isEqualTo(incidentTime);
        assertThat(response.getClaimAmount()).isEqualByComparingTo("4200.00");
        assertThat(response.getStatus()).isEqualTo(ClaimStatus.AI_REVIEW_COMPLETED);
        assertThat(response.getDescription()).isEqualTo("Rear bumper minor collision damage via transport vehicle mismatch.");
        assertThat(response.getCreatedAt()).isEqualTo(creationTime);
        assertThat(response.getUpdatedAt()).isEqualTo(modificationTime);
    }

    /**
     * Verifies that the partial patch configuration updates only non-null values transmitted via
     * {@link UpdateClaimRequest}, securely locking un-transmitted properties from destructive overwrites.
     */
    @Test
    @DisplayName("Should replace target entity fields only when non-null properties are explicitly supplied in the request patch")
    void shouldUpdateOnlyNonNullFields() {
        // Given
        UUID baselineId = UUID.randomUUID();
        LocalDateTime originalIncidentDate = LocalDateTime.of(2026, 4, 1, 12, 0);
        BigDecimal originalAmount = new BigDecimal("850.00");

        Claim existingClaim = Claim.builder()
                .id(baselineId)
                .policyNumber("POL-555")
                .claimantName("Robert Jenkins")
                .claimType("HEALTH")
                .incidentDate(originalIncidentDate)
                .claimAmount(originalAmount)
                .status(ClaimStatus.UNDER_REVIEW)
                .description("Initial outpatient checkup narrative statement.")
                .build();

        UpdateClaimRequest patchRequest = UpdateClaimRequest.builder()
                .claimantName("Bob Jenkins Jr.")
                .description("Modified outpatient emergency operational surgical observation script update.")
                .policyNumber(null)
                .claimType(null)
                .incidentDate(null)
                .claimAmount(null)
                .build();

        // When
        mapper.updateClaimFromRequest(patchRequest, existingClaim);

        // Then
        assertThat(existingClaim.getId()).isEqualTo(baselineId); // Invariant
        assertThat(existingClaim.getClaimantName()).isEqualTo("Bob Jenkins Jr."); // Updated
        assertThat(existingClaim.getDescription()).isEqualTo("Modified outpatient emergency operational surgical observation script update."); // Updated
        
        // Assert Untouched/Preserved Properties
        assertThat(existingClaim.getPolicyNumber()).isEqualTo("POL-555");
        assertThat(existingClaim.getClaimType()).isEqualTo("HEALTH");
        assertThat(existingClaim.getIncidentDate()).isEqualTo(originalIncidentDate);
        assertThat(existingClaim.getClaimAmount()).isEqualByComparingTo(originalAmount);
        assertThat(existingClaim.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
    }

    /**
     * Confirms that a total-null instance payload mapped against an active data-container target leaves
     * all target properties entirely invariant without zeroing out historical entity content.
     */
    @Test
    @DisplayName("Should completely preserve target entity configuration state when the patch configuration holds exclusively null inputs")
    void shouldIgnoreNullFieldsDuringPartialUpdate() {
        // Given
        UUID trackingId = UUID.randomUUID();
        LocalDateTime incidentTime = LocalDateTime.of(2026, 1, 10, 8, 15);
        BigDecimal amount = new BigDecimal("99500.00");

        Claim baselineClaim = Claim.builder()
                .id(trackingId)
                .policyNumber("POL-STABLE")
                .claimantName("Invariant Legacy Enterprise")
                .claimType("COMMERCIAL")
                .incidentDate(incidentTime)
                .claimAmount(amount)
                .status(ClaimStatus.MANUAL_REVIEW_REQUIRED)
                .description("Fixed warehouse assets logistics ventilation thermal damage claim profile.")
                .build();

        UpdateClaimRequest totalNullRequest = new UpdateClaimRequest(); // Every bean field initialized to null

        // When
        mapper.updateClaimFromRequest(totalNullRequest, baselineClaim);

        // Then
        assertThat(baselineClaim.getId()).isEqualTo(trackingId);
        assertThat(baselineClaim.getPolicyNumber()).isEqualTo("POL-STABLE");
        assertThat(baselineClaim.getClaimantName()).isEqualTo("Invariant Legacy Enterprise");
        assertThat(baselineClaim.getClaimType()).isEqualTo("COMMERCIAL");
        assertThat(baselineClaim.getIncidentDate()).isEqualTo(incidentTime);
        assertThat(baselineClaim.getClaimAmount()).isEqualByComparingTo(amount);
        assertThat(baselineClaim.getStatus()).isEqualTo(ClaimStatus.MANUAL_REVIEW_REQUIRED);
        assertThat(baselineClaim.getDescription()).isEqualTo("Fixed warehouse assets logistics ventilation thermal damage claim profile.");
    }
}