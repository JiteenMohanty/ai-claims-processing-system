package com.jiteen.claims.claim.domain.enums;

/**
 * Represents the lifecycle state machine of an insurance claim within the
 * AI-Powered Insurance Claims Processing Platform[cite: 1, 4, 35].
 * * This enum governs the progression transitions of a claim from its initial ingestion 
 * through AI automated analysis, human verification, and ultimate settlement or closure[cite: 32, 37].
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
public enum ClaimStatus {

    /**
     * Initial preparation state. The claim is being drafted by the claimant 
     * or system and has not yet been officially submitted for evaluation[cite: 25, 32].
     */
    DRAFT,

    /**
     * The claim has been formally submitted by the customer and ingested into the platform[cite: 25, 32]. 
     * It is awaiting pipeline orchestration and assignment[cite: 32].
     */
    SUBMITTED,

    /**
     * General evaluation state indicating that the claim is undergoing active verification 
     * within the processing workflow[cite: 25, 32].
     */
    UNDER_REVIEW,

    /**
     * Asynchronous AI processing is currently actively executing on the claim[cite: 32, 37]. 
     * This includes text extraction via OCR, document classification, policy metadata analysis, 
     * automated fraud detection checks, and initial risk scoring[cite: 85, 122].
     */
    AI_REVIEW_IN_PROGRESS,

    /**
     * Automated AI intelligence execution has successfully completed[cite: 85, 91]. 
     * Structured metadata, risk assessment matrices, and recommendation scores have been persisted[cite: 83, 85, 122].
     */
    AI_REVIEW_COMPLETED,

    /**
     * Business rules, high fraud risk vectors, or specific policy conditions have triggered 
     * a requirement for manual intervention and dedicated evaluation by a human Claim Officer[cite: 25, 32, 125].
     */
    MANUAL_REVIEW_REQUIRED,

    /**
     * The claim has successfully satisfied all validation constraints, automated criteria, 
     * or administrative oversight checks, and has been cleared for payment processing[cite: 25, 32].
     */
    APPROVED,

    /**
     * The claim submission has been formally denied due to missing information, invalid policies, 
     * confirmed fraudulent indicators, or lack of coverage alignment[cite: 25, 32, 89].
     */
    REJECTED,

    /**
     * Financial disbursement and accounting workflows have been triggered[cite: 37]. 
     * The approved claim amount is currently queued within the banking transaction gateway.
     */
    PAYMENT_PENDING,

    /**
     * Financial settlement is complete. The approved monetary funds have been successfully 
     * transferred and verified as received by the claimant bank account.
     */
    PAID,

    /**
     * Administrative terminal state. The claim lifecycle is fully realized, audited, 
     * and permanently archived within the historical system of record[cite: 12, 26, 37].
     */
    CLOSED;

    /**
     * Determines whether the current state represents a terminal node in the claim workflow 
     * lifecycle from which no further transitions can naturally progress.
     *
     * @return {@code true} if the status is {@link #REJECTED} or {@link #CLOSED}; 
     * {@code false} otherwise.
     */
    public boolean isTerminal() {
        return switch (this) {
            case REJECTED, CLOSED -> true;
            default -> false;
        };
    }

    /**
     * Determines whether the claim is actively undergoing analysis within any segment 
     * of the automated or manual evaluation lifecycle layers.
     *
     * @return {@code true} if the status matches {@link #UNDER_REVIEW}, {@link #AI_REVIEW_IN_PROGRESS}, 
     * {@link #AI_REVIEW_COMPLETED}, or {@link #MANUAL_REVIEW_REQUIRED}; 
     * {@code false} otherwise.
     */
    public boolean isReviewState() {
        return switch (this) {
            case UNDER_REVIEW, AI_REVIEW_IN_PROGRESS, AI_REVIEW_COMPLETED, MANUAL_REVIEW_REQUIRED -> true;
            default -> false;
        };
    }
}