# API Contracts

## Overview

This document defines external and internal APIs for Version 1.

---

# Auth Service APIs

## Register User

POST /api/auth/register

Request:

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}

Response:

{
  "userId": "UUID",
  "message": "User registered successfully"
}

---

## Login

POST /api/auth/login

Request:

{
  "email": "john@example.com",
  "password": "password123"
}

Response:

{
  "accessToken": "JWT_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 3600
}

---

## Get Profile

GET /api/auth/profile

Headers:

Authorization: Bearer <token>

Response:

{
  "userId": "UUID",
  "email": "john@example.com",
  "role": "ROLE_USER"
}

---

# Claim Service APIs

## Create Claim

POST /api/claims

Headers:

Authorization: Bearer <token>

Request:

{
  "claimType": "VEHICLE_ACCIDENT",
  "claimAmount": 5000,
  "incidentDate": "2026-05-30"
}

Response:

{
  "claimId": "UUID",
  "claimNumber": "CLM-2026-001",
  "status": "DRAFT"
}

---

## Submit Claim

POST /api/claims/{claimId}/submit

Response:

{
  "claimId": "UUID",
  "status": "SUBMITTED"
}

---

## Get Claim

GET /api/claims/{claimId}

Response:

{
  "claimId": "UUID",
  "claimNumber": "CLM-2026-001",
  "status": "UNDER_REVIEW"
}

---

## Upload Document

POST /api/claims/{claimId}/documents

Content-Type:

multipart/form-data

Response:

{
  "documentId": "UUID",
  "message": "Document uploaded successfully"
}