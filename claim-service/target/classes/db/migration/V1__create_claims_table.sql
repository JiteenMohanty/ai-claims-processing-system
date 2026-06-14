CREATE TABLE claims (
    id UUID PRIMARY KEY,
    policy_number VARCHAR(100) NOT NULL,
    claimant_name VARCHAR(255) NOT NULL,
    claim_type VARCHAR(100) NOT NULL,
    incident_date TIMESTAMP NOT NULL,
    claim_amount NUMERIC(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);