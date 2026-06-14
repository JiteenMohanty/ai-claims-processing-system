package com.jiteen.claims.claim.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.jiteen.claims.claim.domain.entity.Claim;
import com.jiteen.claims.claim.domain.enums.ClaimStatus;

/**
 * Integration test suite for {@link ClaimRepository} utilizing an embedded or test database environment.
 * <p>
 * This class leverages Spring Boot's {@link DataJpaTest} to configure a minimized JPA-focused application 
 * context for validating custom derived query methods. It explicitly exercises and asserts both positive 
 * and negative constraints, focusing heavily on the platform's standard soft-delete data architecture 
 * (where records with {@code deletedAt != null} must be excluded from operational views).
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@DataJpaTest
@DisplayName("ClaimRepository Integration Tests")
@ActiveProfiles("test")
class ClaimRepositoryTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Verifies that an active, non-deleted claim can be successfully retrieved by its unique identifier.
     */
    @Test
    @DisplayName("Should find active claim by ID when record is not soft-deleted")
    void shouldFindClaimByIdWhenActive() {
        // Given
        Claim activeClaim = createAndPersistActiveClaim("POL-100", "John Doe", ClaimStatus.SUBMITTED, new BigDecimal("1500.00"));

        // When
        Optional<Claim> result = claimRepository.findByIdAndDeletedAtIsNull(activeClaim.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeClaim.getId());
        assertThat(result.get().getPolicyNumber()).isEqualTo("POL-100");
        assertThat(result.get().getDeletedAt()).isNull();
    }

    /**
     * Verifies that a soft-deleted claim cannot be retrieved using the active record lookup method.
     */
    @Test
    @DisplayName("Should return empty optional when looking up a soft-deleted claim by ID")
    void shouldNotFindClaimByIdWhenSoftDeleted() {
        // Given
        Claim softDeletedClaim = createAndPersistSoftDeletedClaim("POL-200", "Jane Doe", ClaimStatus.UNDER_REVIEW, new BigDecimal("2500.00"));

        // When
        Optional<Claim> result = claimRepository.findByIdAndDeletedAtIsNull(softDeletedClaim.getId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Verifies that checking for existence returns true if the claim is active.
     */
    @Test
    @DisplayName("Should return true for existence check when matching active claim exists")
    void shouldReturnTrueWhenExistsByIdAndActive() {
        // Given
        Claim activeClaim = createAndPersistActiveClaim("POL-300", "Alice Smith", ClaimStatus.APPROVED, new BigDecimal("5000.00"));

        // When
        boolean exists = claimRepository.existsByIdAndDeletedAtIsNull(activeClaim.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Verifies that checking for existence returns false if the target record is soft-deleted.
     */
    @Test
    @DisplayName("Should return false for existence check when matching claim is soft-deleted")
    void shouldReturnFalseWhenExistsByIdAndSoftDeleted() {
        // Given
        Claim softDeletedClaim = createAndPersistSoftDeletedClaim("POL-400", "Bob Jones", ClaimStatus.REJECTED, new BigDecimal("750.00"));

        // When
        boolean exists = claimRepository.existsByIdAndDeletedAtIsNull(softDeletedClaim.getId());

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Verifies that checking for existence returns false if the identifier does not match any entry.
     */
    @Test
    @DisplayName("Should return false for existence check when target identifier does not exist")
    void shouldReturnFalseWhenClaimDoesNotExist() {
        // When
        boolean exists = claimRepository.existsByIdAndDeletedAtIsNull(UUID.randomUUID());

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Verifies that claims filtering by status accurately compiles active records while ignoring soft-deleted records.
     */
    @Test
    @DisplayName("Should return active claims matching specified status and exclude soft-deleted ones")
    void shouldReturnClaimsByStatus() {
        // Given
        createAndPersistActiveClaim("POL-501", "Claimant A", ClaimStatus.UNDER_REVIEW, new BigDecimal("100.00"));
        createAndPersistActiveClaim("POL-502", "Claimant B", ClaimStatus.UNDER_REVIEW, new BigDecimal("200.00"));
        createAndPersistActiveClaim("POL-503", "Claimant C", ClaimStatus.APPROVED, new BigDecimal("300.00"));
        createAndPersistSoftDeletedClaim("POL-504", "Claimant D", ClaimStatus.UNDER_REVIEW, new BigDecimal("400.00"));

        // When
        List<Claim> underReviewClaims = claimRepository.findByStatusAndDeletedAtIsNull(ClaimStatus.UNDER_REVIEW);

        // Then
        assertThat(underReviewClaims).hasSize(2);
        assertThat(underReviewClaims).extracting(Claim::getPolicyNumber)
                .containsExactlyInAnyOrder("POL-501", "POL-502");
    }

    /**
     * Verifies that query filtering by specific policy numbers returns accurate active claim sets.
     */
    @Test
    @DisplayName("Should return active claims associated with a given policy number")
    void shouldReturnClaimsByPolicyNumber() {
        // Given
        String targetedPolicy = "POL-999-MATCH";
        createAndPersistActiveClaim(targetedPolicy, "User One", ClaimStatus.SUBMITTED, new BigDecimal("1200.00"));
        createAndPersistActiveClaim(targetedPolicy, "User One Second Loss", ClaimStatus.UNDER_REVIEW, new BigDecimal("4500.00"));
        createAndPersistActiveClaim("POL-DIFFERENT", "User Two", ClaimStatus.SUBMITTED, new BigDecimal("600.00"));
        createAndPersistSoftDeletedClaim(targetedPolicy, "User One Old Soft Deleted", ClaimStatus.REJECTED, new BigDecimal("90.00"));

        // When
        List<Claim> results = claimRepository.findByPolicyNumberAndDeletedAtIsNull(targetedPolicy);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Claim::getClaimantName)
                .containsExactlyInAnyOrder("User One", "User One Second Loss");
    }

    /**
     * Verifies that case-insensitive partial keyword wildcard searching functions correctly against claimant names.
     */
    @Test
    @DisplayName("Should locate active claims utilizing case-insensitive token wildcard string matching")
    void shouldSearchClaimantNameIgnoringCase() {
        // Given
        createAndPersistActiveClaim("POL-601", "Alexander Henderson", ClaimStatus.SUBMITTED, new BigDecimal("1000.00"));
        createAndPersistActiveClaim("POL-602", "alex Smith", ClaimStatus.UNDER_REVIEW, new BigDecimal("2000.00"));
        createAndPersistActiveClaim("POL-603", "VALERIE ALEXANDRIA", ClaimStatus.APPROVED, new BigDecimal("3000.00"));
        createAndPersistActiveClaim("POL-604", "John Doe", ClaimStatus.SUBMITTED, new BigDecimal("4000.00"));
        createAndPersistSoftDeletedClaim("POL-605", "Alex Jones Soft Deleted", ClaimStatus.SUBMITTED, new BigDecimal("500.00"));

        // When
        List<Claim> lowercaseSearch = claimRepository.findByClaimantNameContainingIgnoreCaseAndDeletedAtIsNull("alex");

        // Then
        assertThat(lowercaseSearch).hasSize(3);
        assertThat(lowercaseSearch).extracting(Claim::getClaimantName)
                .containsExactlyInAnyOrder("Alexander Henderson", "alex Smith", "VALERIE ALEXANDRIA");
    }

    /**
     * Verifies that counting calculations accurately aggregate active records for a specific lifecycle phase.
     */
    @Test
    @DisplayName("Should count total active claims resting in specified status and skip soft-deleted entries")
    void shouldCountClaimsByStatus() {
        // Given
        createAndPersistActiveClaim("POL-701", "Alpha", ClaimStatus.AI_REVIEW_IN_PROGRESS, new BigDecimal("150.00"));
        createAndPersistActiveClaim("POL-702", "Beta", ClaimStatus.AI_REVIEW_IN_PROGRESS, new BigDecimal("350.00"));
        createAndPersistActiveClaim("POL-703", "Gamma", ClaimStatus.SUBMITTED, new BigDecimal("950.00"));
        createAndPersistSoftDeletedClaim("POL-704", "Delta", ClaimStatus.AI_REVIEW_IN_PROGRESS, new BigDecimal("100.00"));

        // When
        long count = claimRepository.countByStatusAndDeletedAtIsNull(ClaimStatus.AI_REVIEW_IN_PROGRESS);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    /**
     * Private helper framework method to instantiate, map, and persist an active claim instance.
     */
    private Claim createAndPersistActiveClaim(String policyNumber, String claimantName, ClaimStatus status, BigDecimal amount) {
        Claim claim = buildBaseClaim(policyNumber, claimantName, status, amount);
        claim.setDeletedAt(null);
        return entityManager.persistAndFlush(claim);
    }

    /**
     * Private helper framework method to instantiate, map, and persist a pre-soft-deleted claim instance.
     */
    private Claim createAndPersistSoftDeletedClaim(String policyNumber, String claimantName, ClaimStatus status, BigDecimal amount) {
        Claim claim = buildBaseClaim(policyNumber, claimantName, status, amount);
        claim.setDeletedAt(LocalDateTime.now().minusDays(1));
        return entityManager.persistAndFlush(claim);
    }

    /**
     * Private template builder establishing invariant data profiles required by database constraints.
     */
    private Claim buildBaseClaim(String policyNumber, String claimantName, ClaimStatus status, BigDecimal amount) {
        return Claim.builder()
                .policyNumber(policyNumber)
                .claimantName(claimantName)
                .claimType("AUTO")
                .incidentDate(LocalDateTime.now().minusDays(5))
                .claimAmount(amount)
                .status(status)
                .description("Automated transactional validation query performance assertion payload scenario.")
                .build();
    }
}