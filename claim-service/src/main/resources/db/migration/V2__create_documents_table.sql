-- ============================================================================
-- Flyway Migration: V2__create_documents_table.sql
-- Description: Instantiates the 'documents' relational schema to store tracking 
--              metadata for supporting document assets attached to insurance claims.
-- Platform: AI-Powered Insurance Claims Processing Platform
-- Database Constraints: PostgreSQL 15+ / Hibernate Compatible
-- Archetype: Layered Microservices Cloud-Ready Architecture
-- ============================================================================

-- Create the core documents persistence schema
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    -- Typed as UUID to maintain structural parity and referential type matching with claims(id)
    claim_id UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    upload_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    -- Foreign Key structural constraint mapping documentation link
    CONSTRAINT fk_documents_claim FOREIGN KEY (claim_id) REFERENCES claims(id)
);

-- Schema documentation mappings registered directly within the PostgreSQL catalog engine
COMMENT ON TABLE documents IS 'Stores enterprise metadata profiles and localization pointers for claim attachment file resources.';
COMMENT ON COLUMN documents.id IS 'Surrogate identity primary key tracking for the relative document meta lifecycle.';
COMMENT ON COLUMN documents.claim_id IS 'Relational logical identifier establishing a mandatory link to the parent insurance claim record.';
COMMENT ON COLUMN documents.original_file_name IS 'The authentic file name capture token preserved from consumer web client submission layers.';
COMMENT ON COLUMN documents.stored_file_name IS 'The randomized sanitized unique hash filename mapped internally to circumvent storage boundary collisions.';
COMMENT ON COLUMN documents.content_type IS 'Standard MIME media type indicator used to isolate safe payload parsing execution strategies.';
COMMENT ON COLUMN documents.file_size IS 'The exact technical storage data volume footprint constraint evaluated in bytes.';
COMMENT ON COLUMN documents.storage_path IS 'The fully qualified absolute filesystem directory string or cloud object bucket URI resource locator.';
COMMENT ON COLUMN documents.upload_status IS 'State token capturing the exact ingestion pipeline phase of the file matching DocumentStatus enums.';
COMMENT ON COLUMN documents.created_at IS 'Audit timestamp indicating exactly when the attachment resource entry was instantiated.';
COMMENT ON COLUMN documents.updated_at IS 'Audit timestamp capturing the most recent transactional update applied against this metadata row.';
COMMENT ON COLUMN documents.deleted_at IS 'Soft-delete epoch marker tracking isolating records from standard workspace reads without destructive drops.';

-- Operational optimization indexes targeting common lookup queries and foreign scanning constraints
-- Index to optimize relational entity graph traversal, query joins, and cascade verification runs
CREATE INDEX idx_documents_claim_id ON documents(claim_id);

-- Index to optimize asynchronous processing background tasks and AI batch orchestration pipelines filtering by status vectors
CREATE INDEX idx_documents_upload_status ON documents(upload_status);