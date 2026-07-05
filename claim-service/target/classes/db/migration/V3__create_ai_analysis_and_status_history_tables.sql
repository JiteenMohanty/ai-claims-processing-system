-- ============================================================================
-- Flyway Migration: V3__create_ai_analysis_and_status_history_tables.sql
-- Description: Creates the 'claim_status_history' audit trail table and the
--              'ai_analysis_results' table for persisting AI Processing Service
--              analysis outputs for each insurance claim.
-- Platform: AI-Powered Insurance Claims Processing Platform
-- Database: PostgreSQL 15+
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Table: claim_status_history
-- Purpose: Immutable audit trail tracking every claim lifecycle status
--          transition. Records are append-only and never deleted or modified.
-- ---------------------------------------------------------------------------
CREATE TABLE claim_status_history (
    id          BIGSERIAL       PRIMARY KEY,
    claim_id    UUID            NOT NULL,
    old_status  VARCHAR(50)     NOT NULL,
    new_status  VARCHAR(50)     NOT NULL,
    changed_by  VARCHAR(255),
    changed_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_status_history_claim
        FOREIGN KEY (claim_id) REFERENCES claims(id)
);

COMMENT ON TABLE  claim_status_history             IS 'Immutable audit log of all insurance claim status lifecycle transitions.';
COMMENT ON COLUMN claim_status_history.claim_id    IS 'Reference to the parent insurance claim undergoing the status change.';
COMMENT ON COLUMN claim_status_history.old_status  IS 'Claim lifecycle status value active immediately before this transition.';
COMMENT ON COLUMN claim_status_history.new_status  IS 'Claim lifecycle status value applied as a result of this transition event.';
COMMENT ON COLUMN claim_status_history.changed_by  IS 'Actor identifier (officer ID, system process name) that triggered the transition.';
COMMENT ON COLUMN claim_status_history.changed_at  IS 'Precise timestamp at which the status transition was recorded.';

CREATE INDEX idx_claim_status_history_claim_id
    ON claim_status_history(claim_id);

-- ---------------------------------------------------------------------------
-- Table: ai_analysis_results
-- Purpose: Stores structured AI-generated analysis outputs for each claim.
--          Populated asynchronously by the AI Processing Service after
--          receiving a CLAIM_CREATED Kafka event.
-- ---------------------------------------------------------------------------
CREATE TABLE ai_analysis_results (
    id                      UUID            PRIMARY KEY,
    claim_id                UUID            NOT NULL UNIQUE,
    summary                 TEXT,
    risk_score              INTEGER         NOT NULL,
    recommended_action      VARCHAR(100),
    fraud_indicators        TEXT,
    policy_number           VARCHAR(100),
    claim_amount_extracted  DOUBLE PRECISION,
    incident_date_extracted TIMESTAMP,
    customer_name_extracted VARCHAR(255),
    missing_documents       TEXT,
    processed_at            TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL,
    deleted_at              TIMESTAMP,

    CONSTRAINT fk_ai_results_claim
        FOREIGN KEY (claim_id) REFERENCES claims(id)
);

COMMENT ON TABLE  ai_analysis_results                       IS 'Structured AI analysis results produced by the AI Processing Service per claim.';
COMMENT ON COLUMN ai_analysis_results.claim_id              IS 'One-to-one reference to the parent insurance claim being analysed.';
COMMENT ON COLUMN ai_analysis_results.summary               IS 'AI-generated narrative summary of the claim for officer review.';
COMMENT ON COLUMN ai_analysis_results.risk_score            IS 'Numerical risk score 0-100: 0-30 Low, 31-70 Medium, 71-100 High Risk.';
COMMENT ON COLUMN ai_analysis_results.recommended_action    IS 'AI recommendation: APPROVE, REJECT, MANUAL_REVIEW_REQUIRED, REQUEST_ADDITIONAL_INFORMATION.';
COMMENT ON COLUMN ai_analysis_results.fraud_indicators      IS 'JSON array of detected fraud indicator descriptions.';
COMMENT ON COLUMN ai_analysis_results.missing_documents     IS 'JSON array of document types identified as missing during validation.';
COMMENT ON COLUMN ai_analysis_results.processed_at          IS 'Timestamp when the AI Processing Service completed its analysis workflow.';

CREATE INDEX idx_ai_results_claim_id
    ON ai_analysis_results(claim_id);

CREATE INDEX idx_ai_results_risk_score
    ON ai_analysis_results(risk_score);
