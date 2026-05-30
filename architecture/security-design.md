# Security Architecture

## Overview

The platform uses JWT-based authentication and role-based authorization.

Security goals:

- Secure user authentication
- Secure API access
- Role-based authorization
- Password protection
- Stateless architecture

---

# User Roles

## ROLE_USER

Permissions:

- Create claim
- Update own claim
- Upload documents
- View own claims

---

## ROLE_OFFICER

Permissions:

- Review claims
- Approve claims
- Reject claims
- Request additional information

---

## ROLE_ADMIN

Permissions:

- Manage users
- View analytics
- View audit logs
- System administration

---

# Authentication Flow

User Login

↓

Auth Service

↓

Validate Credentials

↓

Generate JWT Token

↓

Return Token

↓

Client Stores Token

↓

Client Sends Token In Requests

---

# Authorization Flow

Client Request

↓

API Gateway

↓

Validate JWT

↓

Extract User Role

↓

Forward Request

↓

Service Checks Permissions

---

# Password Storage

Passwords are never stored in plain text.

Algorithm:

BCrypt

Example:

password123

↓

$2a$10$EncryptedHash

---

# JWT Token Structure

Contains:

- User ID
- Email
- Role
- Expiration Time

Example:

{
  "sub": "user-id",
  "email": "user@example.com",
  "role": "ROLE_USER"
}

---

# Access Rules

Customer APIs

Accessible By:

ROLE_USER

---

Review APIs

Accessible By:

ROLE_OFFICER

ROLE_ADMIN

---

Admin APIs

Accessible By:

ROLE_ADMIN

---

# Future Enhancements

- Refresh Tokens
- OAuth2
- Multi-Factor Authentication
- API Rate Limiting
- IP Restrictions
- Service-to-Service Authentication

---

# Security Principles

- Least Privilege
- Defense in Depth
- Secure by Default
- Zero Trust Mindset