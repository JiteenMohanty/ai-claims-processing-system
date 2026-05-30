# Claim Lifecycle

## Overview

A claim moves through multiple stages from creation to final resolution.

The lifecycle ensures consistency, auditability, and workflow control.

---

# Claim States

## DRAFT

Description:

Claim has been created but not submitted.

Allowed Actions:

- Edit Claim
- Upload Documents
- Delete Claim
- Submit Claim

Next State:

SUBMITTED

---

## SUBMITTED

Description:

Customer has submitted claim for processing.

System Actions:

- Validate claim
- Store documents
- Publish Kafka event

Next State:

AI_ANALYZING

---

## AI_ANALYZING

Description:

AI Processing Service analyzes uploaded documents.

Activities:

- OCR extraction
- Metadata extraction
- Fraud analysis
- Risk scoring
- Claim summarization

Next State:

UNDER_REVIEW

---

## UNDER_REVIEW

Description:

Claim officer reviews claim.

Available Information:

- Uploaded documents
- AI summary
- Risk score
- Fraud indicators

Possible Outcomes:

- APPROVED
- REJECTED
- MORE_INFO_REQUIRED

---

## MORE_INFO_REQUIRED

Description:

Additional documentation required.

Customer Actions:

- Upload documents
- Update claim

Next State:

UNDER_REVIEW

---

## APPROVED

Description:

Claim approved.

System Actions:

- Notify customer
- Generate audit record
- Close workflow

Terminal State

---

## REJECTED

Description:

Claim rejected.

System Actions:

- Notify customer
- Generate audit record

Terminal State

---

# State Diagram

DRAFT
|
v
SUBMITTED
|
v
AI_ANALYZING
|
v
UNDER_REVIEW
|
+-------------------------+
|                         |
v                         v
APPROVED            REJECTED
|
v
END

UNDER_REVIEW
|
v
MORE_INFO_REQUIRED
|
v
UNDER_REVIEW