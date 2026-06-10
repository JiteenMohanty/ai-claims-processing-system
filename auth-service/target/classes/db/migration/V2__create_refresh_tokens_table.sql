-- =============================================================================
-- Flyway Migration: V2__create_refresh_tokens_table.sql
-- -----------------------------------------------------------------------------
-- Purpose:
--   Creates the "refresh_tokens" table, which stores JWT refresh tokens issued
--   to authenticated users. Each refresh token belongs to a single user, has an
--   expiration timestamp, and may be explicitly revoked to invalidate it prior
--   to its natural expiry. This supports re-issuing access tokens without
--   requiring the user to re-authenticate.
--
-- Target Database:
--   PostgreSQL 16
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Table: refresh_tokens
-- Stores persisted refresh tokens along with ownership, expiry, and revocation
-- state, plus standard audit timestamps.
-- -----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          UUID            NOT NULL,
    token       VARCHAR(1000)   NOT NULL,
    user_id     UUID            NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,    

    -- Primary key uniquely identifying each refresh token record.
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),

    -- Ensures that each token value is unique across all records.
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),

    -- Associates each refresh token with its owning user. Deleting a user
    -- cascades to remove all of their refresh tokens.
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- Index: idx_refresh_tokens_user_id
-- Accelerates lookups of all refresh tokens belonging to a specific user, for
-- example when revoking all tokens for a user.
-- -----------------------------------------------------------------------------
CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

-- -----------------------------------------------------------------------------
-- Index: idx_refresh_tokens_expires_at
-- Accelerates expiry-based queries, such as purging expired tokens during
-- scheduled cleanup operations.
-- -----------------------------------------------------------------------------
CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);

-- -----------------------------------------------------------------------------
-- Index: idx_refresh_tokens_revoked
-- Accelerates filtering of tokens by revocation state when validating tokens or
-- identifying revoked records.
-- -----------------------------------------------------------------------------
CREATE INDEX idx_refresh_tokens_revoked
    ON refresh_tokens (revoked);