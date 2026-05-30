# Database Schema Design

## Overview

The platform uses PostgreSQL as the primary relational database.

The initial design focuses on:

- User Management
- Claim Management
- Document Management
- Claim Status Tracking
- AI Analysis Results

---

# users

Purpose:

Store user accounts.

Columns:

id (UUID)

email

password

first_name

last_name

role

created_at

updated_at

---

# claims

Purpose:

Store insurance claims.

Columns:

id (UUID)

claim_number

user_id

claim_type

status

risk_score

claim_amount

incident_date

created_at

updated_at

---

# claim_documents

Purpose:

Store uploaded document metadata.

Columns:

id (UUID)

claim_id

file_name

file_type

file_url

uploaded_at

---

# claim_status_history

Purpose:

Track status transitions.

Columns:

id (UUID)

claim_id

old_status

new_status

changed_by

changed_at

---

# ai_analysis_results

Purpose:

Store AI-generated results.

Columns:

id (UUID)

claim_id

summary

risk_score

recommended_action

fraud_indicators

policy_number

claim_amount_extracted

incident_date_extracted

customer_name_extracted

missing_documents

created_at

---

# Relationships

users

1

↓

many

claims

---

claims

1

↓

many

claim_documents

---

claims

1

↓

many

claim_status_history

---

claims

1

↓

1

ai_analysis_results