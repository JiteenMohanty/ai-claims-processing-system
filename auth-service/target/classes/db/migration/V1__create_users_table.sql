-- V1__create_users_table.sql
-- Creates the users table for authentication and authorization.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    role VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',

    last_login_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted_at TIMESTAMP NULL,

    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT chk_users_email_not_blank
        CHECK (length(btrim(email)) > 0),

    CONSTRAINT chk_users_password_hash_not_blank
        CHECK (length(btrim(password_hash)) > 0),

    CONSTRAINT chk_users_first_name_not_blank
        CHECK (length(btrim(first_name)) > 0),

    CONSTRAINT chk_users_last_name_not_blank
        CHECK (length(btrim(last_name)) > 0),

    CONSTRAINT chk_users_role_allowed
        CHECK (
            role IN (
                'ADMIN',
                'CLAIMS_OFFICER',
                'CUSTOMER'
            )
        ),

    CONSTRAINT chk_users_status_allowed
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'LOCKED',
                'PENDING_VERIFICATION'
            )
        ),

    CONSTRAINT chk_users_updated_at_not_before_created_at
        CHECK (updated_at >= created_at),

    CONSTRAINT chk_users_deleted_at_not_before_created_at
        CHECK (
            deleted_at IS NULL
            OR deleted_at >= created_at
        )
);

COMMENT ON TABLE users IS
'Stores user accounts used for authentication, authorization, and account lifecycle management.';

COMMENT ON COLUMN users.id IS
'Primary key of the user account represented as a UUID.';

COMMENT ON COLUMN users.email IS
'Unique email address used as the user login identifier.';

COMMENT ON COLUMN users.password_hash IS
'Securely hashed user password. Raw passwords must never be stored.';

COMMENT ON COLUMN users.first_name IS
'User first name.';

COMMENT ON COLUMN users.last_name IS
'User last name.';

COMMENT ON COLUMN users.role IS
'User authorization role. Allowed values: ADMIN, CLAIMS_OFFICER, CUSTOMER.';

COMMENT ON COLUMN users.status IS
'User account status. Allowed values: ACTIVE, INACTIVE, LOCKED, PENDING_VERIFICATION.';

COMMENT ON COLUMN users.last_login_at IS
'Timestamp of the user account last successful login.';

COMMENT ON COLUMN users.created_at IS
'Timestamp when the user account record was created.';

COMMENT ON COLUMN users.updated_at IS
'Timestamp when the user account record was last updated.';

COMMENT ON COLUMN users.deleted_at IS
'Timestamp when the user account was soft deleted. NULL means active record.';

CREATE INDEX idx_users_email
ON users (email);

CREATE UNIQUE INDEX idx_users_email_lower
ON users (LOWER(email));

CREATE INDEX idx_users_role
ON users (role);

CREATE INDEX idx_users_status
ON users (status);

CREATE INDEX idx_users_created_at
ON users (created_at);

CREATE INDEX idx_users_deleted_at
ON users (deleted_at);