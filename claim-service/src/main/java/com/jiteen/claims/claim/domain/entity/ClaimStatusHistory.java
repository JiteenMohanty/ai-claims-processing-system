package com.jiteen.claims.claim.domain.entity;

import com.jiteen.claims.claim.domain.enums.ClaimStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a single state transition record within the insurance
 * claim lifecycle audit trail.
 *
 * <p>
 * Every time a claim's {@link ClaimStatus} changes, a corresponding
 * {@code ClaimStatusHistory} record is created and persisted. This provides
 * a complete, immutable audit log of all status transitions, supporting
 * compliance reporting, dispute resolution, and regulatory review workflows.
 * </p>
 *
 * <p>
 * Unlike the {@link Claim} entity, status history records are never soft-deleted
 * or modified after creation; they represent factual historical state snapshots.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Entity
@Table(name = "claim_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimStatusHistory {

    /**
     * Surrogate numeric primary key for the status history record.
     * Uses database-managed sequence generation for simplicity and performance.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key reference to the parent insurance claim whose status
     * transition this record documents.
     */
    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    /**
     * The claim lifecycle status that was active immediately before this
     * transition was applied.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false, length = 50)
    private ClaimStatus oldStatus;

    /**
     * The claim lifecycle status that was assigned as a result of this
     * transition event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private ClaimStatus newStatus;

    /**
     * Identifier of the actor (system component, claim officer ID, or automated
     * process name) that triggered the status transition.
     */
    @Column(name = "changed_by", length = 255)
    private String changedBy;

    /**
     * Precise timestamp at which the status transition was recorded.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
