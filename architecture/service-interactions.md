# Service Interactions

## Overview

This document defines service responsibilities, ownership boundaries, and communication patterns.

---

# Service Ownership

## Auth Service

Owns:

- users
- roles
- refresh_tokens

Responsibilities:

- Authentication
- Authorization
- JWT Management

Communication:

REST APIs

---

## Claim Service

Owns:

- claims
- claim_documents
- claim_status_history

Responsibilities:

- Claim lifecycle management
- Document metadata management
- Claim workflow

Communication:

REST APIs
Kafka Events

---

## AI Processing Service

Owns:

- ai_analysis_results

Responsibilities:

- OCR processing
- Metadata extraction
- Claim summarization
- Fraud detection
- Risk scoring

Communication:

Kafka Events

---

## Notification Service

Owns:

- notification_logs

Responsibilities:

- Email notifications
- SMS notifications
- Customer communication

Communication:

Kafka Events

---

# Communication Matrix

Auth Service

→ PostgreSQL

---

Claim Service

→ PostgreSQL

→ AWS S3

→ Kafka

---

AI Processing Service

→ AWS S3

→ PostgreSQL

→ Kafka

→ OpenAI API

---

Notification Service

→ Kafka

→ Email Provider

---

# Data Ownership Rules

Rule 1:

Services may only directly modify their own data.

---

Rule 2:

Cross-service communication must occur through APIs or Kafka.

---

Rule 3:

Services never directly access another service's database.

---

# Example

Correct:

Claim Service
→ Kafka Event
→ AI Service

Incorrect:

Claim Service
→ Direct SQL Query
→ AI Service Database

---

# Benefits

- Service isolation
- Better scalability
- Independent deployments
- Reduced coupling
- Enterprise architecture alignment