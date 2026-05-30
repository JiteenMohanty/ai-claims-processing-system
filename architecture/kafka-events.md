# Kafka Events Architecture

## Overview

Kafka is used to enable asynchronous communication between microservices.

Benefits:

- Decoupled services
- Improved scalability
- Fault tolerance
- Event replay capability
- Better system responsiveness

---

# Event Flow

Customer Creates Claim

↓

Claim Service

↓

CLAIM_CREATED

↓

AI Processing Service

↓

AI_ANALYSIS_COMPLETED

↓

Claim Service

↓

UNDER_REVIEW

↓

Claim Officer Decision

↓

CLAIM_APPROVED
or
CLAIM_REJECTED

↓

Notification Service

---

# Kafka Topics

## claim-created-topic

Purpose:

New claim submitted.

Producer:

Claim Service

Consumer:

AI Processing Service

---

## ai-analysis-completed-topic

Purpose:

AI analysis completed.

Producer:

AI Processing Service

Consumer:

Claim Service

---

## claim-approved-topic

Purpose:

Claim approved.

Producer:

Claim Service

Consumer:

Notification Service

---

## claim-rejected-topic

Purpose:

Claim rejected.

Producer:

Claim Service

Consumer:

Notification Service

---

# Event Definitions

## CLAIM_CREATED

Producer:

Claim Service

Payload:

{
  "claimId": "UUID",
  "claimNumber": "CLM-2026-001",
  "claimType": "VEHICLE_ACCIDENT",
  "userId": "UUID",
  "submittedAt": "timestamp"
}

---

## AI_ANALYSIS_COMPLETED

Producer:

AI Processing Service

Payload:

{
  "claimId": "UUID",
  "riskScore": 75,
  "summary": "Accident claim involving rear-end collision.",
  "fraudIndicators": [
    "Missing repair invoice"
  ],
  "processedAt": "timestamp"
}

---

## CLAIM_APPROVED

Producer:

Claim Service

Payload:

{
  "claimId": "UUID",
  "approvedBy": "OFFICER_ID",
  "approvedAt": "timestamp"
}

---

## CLAIM_REJECTED

Producer:

Claim Service

Payload:

{
  "claimId": "UUID",
  "rejectedBy": "OFFICER_ID",
  "reason": "Insufficient documentation",
  "rejectedAt": "timestamp"
}

---

# Future Events

DOCUMENT_UPLOADED

MORE_INFO_REQUIRED

CLAIM_STATUS_CHANGED

NOTIFICATION_SENT

AUDIT_EVENT_CREATED