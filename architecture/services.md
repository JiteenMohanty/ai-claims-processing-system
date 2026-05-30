# Services Architecture

## 1. Auth Service

### Purpose

Responsible for authentication and authorization.

### Responsibilities

- User registration
- User login
- JWT token generation
- JWT validation
- Role management
- Password management

### Database Tables

- users
- roles
- refresh_tokens

### APIs

POST /api/auth/register

POST /api/auth/login

POST /api/auth/refresh

GET /api/auth/profile

---

## 2. Claim Service

### Purpose

Core business service responsible for claim management.

### Responsibilities

- Create claim
- Update claim
- View claim
- Claim workflow management
- Document metadata storage
- Status tracking

### Database Tables

- claims
- claim_documents
- claim_status_history

### APIs

POST /api/claims

GET /api/claims/{id}

PUT /api/claims/{id}

GET /api/claims

---

## 3. AI Processing Service

### Purpose

Analyze claim documents using OCR and AI.

### Responsibilities

- OCR extraction
- Claim summarization
- Fraud detection
- Risk scoring
- Claim classification

### Kafka Consumer Events

- CLAIM_CREATED
- DOCUMENT_UPLOADED

### Kafka Producer Events

- AI_ANALYSIS_COMPLETED

---

## 4. Notification Service

### Purpose

Send customer communications.

### Responsibilities

- Email notifications
- SMS notifications
- Status updates
- Approval notifications

### Kafka Consumer Events

- CLAIM_APPROVED
- CLAIM_REJECTED
- MORE_INFO_REQUIRED

---

## 5. API Gateway

### Purpose

Single entry point for all client requests.

### Responsibilities

- Request routing
- Authentication validation
- Rate limiting
- Request logging

---

## Future Service: Audit Service

### Responsibilities

- Audit logs
- User activity tracking
- Compliance reporting
- Change history