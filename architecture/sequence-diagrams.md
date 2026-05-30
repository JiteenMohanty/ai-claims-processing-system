# Sequence Diagrams

## User Registration Flow

User
|
v

API Gateway
|
v

Auth Service
|
v

PostgreSQL

Flow:

1. User submits registration request.
2. API Gateway forwards request.
3. Auth Service validates request.
4. Password is encrypted using BCrypt.
5. User record is saved.
6. Success response returned.

---

## Login Flow

User
|
v

API Gateway
|
v

Auth Service
|
v

PostgreSQL

Flow:

1. User submits credentials.
2. Auth Service validates email/password.
3. JWT token generated.
4. JWT returned to client.
5. Client stores token.

---

## Claim Creation Flow

User
|
v

API Gateway
|
v

Claim Service
|
v

PostgreSQL

Flow:

1. User creates claim.
2. Claim Service validates request.
3. Claim record created.
4. Claim status = DRAFT.
5. Response returned.

---

## Document Upload Flow

User
|
v

API Gateway
|
v

Claim Service
|
v

Object Storage (Future AWS S3)

Flow:

1. User uploads document.
2. File validated.
3. File stored.
4. Metadata saved.
5. Upload confirmation returned.

---

## Claim Submission Flow

User
|
v

API Gateway
|
v

Claim Service
|
v

Kafka

Flow:

1. User submits claim.
2. Claim status changed to SUBMITTED.
3. CLAIM_CREATED event published.
4. Response returned immediately.

---

## AI Analysis Flow

Kafka
|
v

AI Processing Service
|
v

OCR Engine
|
v

LLM Provider
|
v

PostgreSQL

Flow:

1. CLAIM_CREATED event received.
2. Documents loaded.
3. OCR extracts text.
4. AI analyzes content.
5. Risk score calculated.
6. Fraud indicators identified.
7. Metadata extracted.
8. Results saved.
9. AI_ANALYSIS_COMPLETED event published.

---

## Claim Review Flow

Claim Officer
|
v

API Gateway
|
v

Claim Service

Flow:

1. Officer opens claim.
2. Claim details loaded.
3. AI results loaded.
4. Officer reviews information.
5. Officer approves or rejects claim.

---

## Claim Approval Flow

Officer
|
v

Claim Service
|
v

Kafka
|
v

Notification Service

Flow:

1. Officer approves claim.
2. Claim status updated.
3. CLAIM_APPROVED event published.
4. Notification Service receives event.
5. Customer notified.

---

## Claim Rejection Flow

Officer
|
v

Claim Service
|
v

Kafka
|
v

Notification Service

Flow:

1. Officer rejects claim.
2. Claim status updated.
3. CLAIM_REJECTED event published.
4. Customer notified.