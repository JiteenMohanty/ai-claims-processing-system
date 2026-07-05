-- Phase 6: Add storage provider tracking to documents table
-- Identifies whether a document binary is stored on local disk ('local')
-- or in AWS S3 ('s3'), enabling the AI Processing Service to determine
-- whether Amazon Textract OCR is applicable for that document.

ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS storage_provider VARCHAR(20) NOT NULL DEFAULT 'local';

COMMENT ON COLUMN documents.storage_provider IS
    'Storage backend identifier: ''local'' = local filesystem, ''s3'' = AWS S3';
