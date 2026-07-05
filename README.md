# AI-Powered Insurance Claims Processing Platform

A production-grade, event-driven microservices platform that automates the full insurance claim lifecycle — from customer submission through AI-assisted analysis to officer review and customer notification.

Built as a portfolio showcase of enterprise Java backend engineering: Spring Boot 3.5, Kafka, Redis, PostgreSQL, Spring Cloud Gateway, Resilience4j, and Docker.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Client Applications                          │
│                   (Web / Mobile / API Consumers)                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTPS
                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       API Gateway  :8080                             │
│        JWT Authentication · Rate Limiting · CORS · Routing          │
└───────────┬─────────────────────────────────┬────────────────────────┘
            │                                 │
            ▼                                 ▼
┌───────────────────────┐         ┌───────────────────────────────────┐
│  Auth Service  :8081  │         │      Claim Service  :8082         │
│                       │         │                                   │
│  - User registration  │         │  - Claim CRUD & lifecycle         │
│  - JWT issue/refresh  │         │  - Document upload (S3 / local)   │
│  - Token blacklisting │         │  - Status history                 │
│  - Redis user cache   │         │  - Redis claim cache              │
│                       │         │  - Resilience4j circuit breaker   │
└──────────┬────────────┘         └────────────────┬──────────────────┘
           │ PostgreSQL                             │ PostgreSQL
           │ (auth_service DB)                      │ (claim_service DB)
           │                                        │
           │            Apache Kafka                │
           │       ┌────────────────────┐           │
           │       │  claim-created      │◄──────────┘
           │       │  claim-approved     │
           │       │  claim-rejected     │
           │       │  document-uploaded  │
           │       └──────┬─────────┬───┘
           │              │         │
           ▼              ▼         ▼
┌──────────────────┐   ┌──────────────────────────────────────┐
│  AI Processing   │   │       Notification Service  :8084    │
│  Service  :8083  │   │                                      │
│                  │   │  - Consumes claim-approved/rejected  │
│  - OCR pipeline  │   │  - Dispatches customer emails        │
│  - Risk scoring  │   └──────────────────────────────────────┘
│  - Fraud detect  │
│  - GPT analysis  │
└──────────────────┘
```

**Shared infrastructure:** Redis (distributed cache) · Kafka (KRaft, no ZooKeeper) · Kafka UI

---

## Technology Stack

| Category            | Technology                                    | Version   |
|---------------------|-----------------------------------------------|-----------|
| Language            | Java                                          | 21 (LTS)  |
| Framework           | Spring Boot                                   | 3.5.0     |
| API Gateway         | Spring Cloud Gateway                          | 2024.0.1  |
| Messaging           | Apache Kafka (KRaft)                          | 7.6.0     |
| Databases           | PostgreSQL                                    | 16        |
| Caching             | Redis (Lettuce client)                        | 7.2       |
| Migrations          | Flyway                                        | managed   |
| Resilience          | Resilience4j (circuit breakers, retries)      | 2.2.0     |
| Security            | Spring Security + JJWT                        | 0.12.6    |
| DTO Mapping         | MapStruct                                     | 1.6.3     |
| API Documentation   | SpringDoc OpenAPI (Swagger UI)                | 2.8.9     |
| Containerisation    | Docker (multi-stage builds) + Docker Compose  | -         |
| Testing             | JUnit 5, Mockito, Testcontainers, EmbeddedKafka | -       |
| CI                  | GitHub Actions                                | -         |
| AI / OCR            | OpenAI GPT · AWS Textract · Simulated fallback | -        |
| Storage             | AWS S3 / local filesystem (configurable)      | -         |

---

## Services

| Service                | Port | Description                                                     |
|------------------------|------|-----------------------------------------------------------------|
| `api-gateway`          | 8080 | Single ingress: JWT validation, routing, CORS, rate limiting    |
| `auth-service`         | 8081 | User registration, JWT issue/refresh, token blacklisting        |
| `claim-service`        | 8082 | Full claim lifecycle, document upload, AI analysis results      |
| `ai-processing-service`| 8083 | OCR → risk scoring → fraud detection → GPT summary pipeline    |
| `notification-service` | 8084 | Email dispatch on claim approval/rejection                      |
| Kafka UI               | 8090 | Topic and consumer group monitoring                             |

---

## Quick Start — Docker Compose

### Prerequisites
- Docker Desktop 4.x or Docker Engine 24+
- 4 GB of RAM available for containers

### 1. Clone and configure

```bash
git clone <repo-url>
cd ai-claims-processing-system

cp .env.example .env
# Open .env and set at minimum:
#   JWT_SECRET=<random 64-char string>
#   POSTGRES_AUTH_PASSWORD=<your password>
#   POSTGRES_CLAIM_PASSWORD=<your password>
```

### 2. Start the full stack

```bash
docker compose up -d
```

Services start in dependency order. The full stack takes approximately 90 seconds to become healthy on a first run (Maven downloads dependencies inside the build stage).

### 3. Verify

```bash
# All containers healthy
docker compose ps

# API Gateway health
curl http://localhost:8080/actuator/health

# Swagger UIs
open http://localhost:8081/swagger-ui.html   # Auth Service
open http://localhost:8082/swagger-ui.html   # Claim Service

# Kafka topic monitoring
open http://localhost:8090
```

### Stop

```bash
docker compose down          # stop containers, keep volumes
docker compose down -v       # stop + delete all data volumes
```

---

## Quick Start — Local Development (no Docker)

### Prerequisites
- JDK 21
- Maven 3.9+
- PostgreSQL 16 running locally
- Redis 7 running locally (`redis-server`)
- Kafka running locally (or use `docker-compose.infrastructure.yml`)

### 1. Start infrastructure only

```bash
docker compose -f docker-compose.infrastructure.yml up -d   # Kafka only
# Plus local PostgreSQL + Redis already running
```

### 2. Create databases

```sql
CREATE DATABASE auth_service;
CREATE DATABASE claim_service;
```

### 3. Set environment variables

```bash
export JWT_SECRET=local-dev-secret-key-at-least-32-characters
```

### 4. Run each service

```bash
# Auth Service
cd auth-service && mvn spring-boot:run

# Claim Service (new terminal)
cd claim-service && mvn spring-boot:run

# AI Processing Service (new terminal)
cd ai-processing-service && mvn spring-boot:run

# Notification Service (new terminal)
cd notification-service && mvn spring-boot:run

# API Gateway (new terminal)
cd api-gateway && mvn spring-boot:run
```

---

## API Endpoints

All requests go through the API Gateway at `http://localhost:8080`.

Public endpoints (no token required):

| Method | Path                           | Description           |
|--------|--------------------------------|-----------------------|
| POST   | `/api/v1/auth/register`        | Register new user     |
| POST   | `/api/v1/auth/login`           | Obtain JWT tokens     |
| POST   | `/api/v1/auth/refresh`         | Refresh access token  |
| POST   | `/api/v1/auth/logout`          | Revoke refresh token  |

Protected endpoints (Bearer token required):

| Method | Path                              | Description                         |
|--------|-----------------------------------|-------------------------------------|
| GET    | `/api/v1/auth/profile`            | Get current user profile            |
| POST   | `/api/v1/claims`                  | Submit a new claim                  |
| GET    | `/api/v1/claims`                  | List all claims (paginated)         |
| GET    | `/api/v1/claims/{id}`             | Get claim by ID                     |
| PUT    | `/api/v1/claims/{id}`             | Update a claim                      |
| DELETE | `/api/v1/claims/{id}`             | Soft-delete a claim                 |
| POST   | `/api/v1/claims/{id}/approve`     | Approve a claim (officer role)      |
| POST   | `/api/v1/claims/{id}/reject`      | Reject a claim (officer role)       |
| POST   | `/api/v1/documents/upload/{id}`   | Upload document for a claim         |
| GET    | `/api/v1/documents/claim/{id}`    | List documents for a claim          |
| GET    | `/api/v1/ai-analysis/{claimId}`   | Get AI analysis result              |

Full Swagger documentation available per-service at `/swagger-ui.html`.

---

## Kafka Events

| Topic                      | Producer       | Consumer(s)                      | Payload                        |
|----------------------------|----------------|----------------------------------|--------------------------------|
| `claim-created-topic`      | claim-service  | ai-processing-service            | `ClaimCreatedEvent`            |
| `claim-approved-topic`     | claim-service  | notification-service             | `ClaimApprovedEvent`           |
| `claim-rejected-topic`     | claim-service  | notification-service             | `ClaimRejectedEvent`           |
| `document-uploaded-topic`  | claim-service  | ai-processing-service            | `DocumentUploadedEvent`        |
| `ai-analysis-completed-topic` | ai-processing-service | claim-service           | `AiAnalysisCompletedEvent`     |

---

## Running Tests

Each service has an independent test suite:

```bash
# Run all tests for a service
cd claim-service && mvn test

# All services (from project root — requires Maven Wrapper or scripts)
for svc in auth-service claim-service ai-processing-service notification-service api-gateway; do
  echo "=== $svc ===" && cd $svc && mvn test && cd ..
done
```

Test isolation strategy:

| Service                  | DB Strategy                                    | Kafka Strategy         | Cache |
|--------------------------|------------------------------------------------|------------------------|-------|
| `auth-service`           | `@Testcontainers` PostgreSQL (skipped without Docker) | N/A               | disabled (`cache.type: none`) |
| `claim-service`          | H2 in-memory (`MODE=PostgreSQL`)               | `@EmbeddedKafka`       | disabled |
| `ai-processing-service`  | N/A (no DB)                                    | `@EmbeddedKafka`       | N/A   |
| `notification-service`   | N/A (no DB)                                    | `@EmbeddedKafka`       | N/A   |
| `api-gateway`            | N/A (no DB)                                    | N/A                    | N/A   |

---

## Project Structure

```
ai-claims-processing-system/
├── api-gateway/                 Spring Cloud Gateway (port 8080)
├── auth-service/                JWT auth + user management (port 8081)
├── claim-service/               Core claims engine (port 8082)
├── ai-processing-service/       OCR + AI analysis pipeline (port 8083)
├── notification-service/        Email notifications (port 8084)
│
├── architecture/                Architecture decision records, diagrams, schemas
├── docs/                        Additional documentation
│
├── docker-compose.yml           Full-stack compose (all services + infrastructure)
├── docker-compose.infrastructure.yml  Infrastructure-only (Kafka + Kafka UI)
├── .env.example                 Environment variable template
└── .github/workflows/ci.yml     GitHub Actions CI pipeline
```

---

## Key Design Decisions

**Database-per-service** — `auth_service` and `claim_service` each own their PostgreSQL database. No shared schema, no cross-service JOINs. Services communicate exclusively through Kafka events.

**Event-driven AI pipeline** — Document upload publishes `DocumentUploadedEvent` to Kafka. The AI Processing Service consumes it asynchronously, runs OCR + GPT analysis, and publishes `AiAnalysisCompletedEvent`. The Claim Service consumes that to update the claim status. The entire pipeline is decoupled and independently scalable.

**Gateway-level authentication** — The API Gateway validates JWT signatures before forwarding requests. Downstream services receive `X-User-Email` and `X-User-Role` headers instead of raw tokens, eliminating per-service token parsing.

**Resilience4j circuit breakers on Kafka** — `KafkaClaimEventPublisher` is decorated with `@CircuitBreaker` and `@Retry`. If Kafka is unavailable, the circuit opens after 50 % failure rate over a 10-call sliding window and routes to fallback logging. Claim operations succeed; event delivery is best-effort.

**Redis caching with TTL** — Claims are cached for 15 minutes, claim lists for 5 minutes, user credentials for 30 minutes. Cache eviction is triggered on every write operation and propagated programmatically for consumer-driven updates that bypass the service layer.

**MDC correlation IDs** — Every HTTP request receives an `X-Correlation-Id` (propagated from the gateway or generated if absent). The ID is injected into SLF4J MDC so every log line across the request lifecycle carries the same identifier, enabling cross-service log correlation.

**Soft deletes** — All entities extend `AuditBaseEntity` with `createdAt`, `updatedAt`, and `deletedAt`. No hard deletes. All queries filter on `deletedAt IS NULL`.

**Configurable storage** — `StorageService` is an interface. `LocalStorageService` activates by default (`matchIfMissing=true`). `S3StorageService` activates when `storage.provider=s3`. Switch at runtime via environment variable — no code change required.

---

## Architecture Documentation

Detailed design documents are in [`architecture/`](architecture/):

- [`system-overview.md`](architecture/system-overview.md) — High-level platform overview
- [`services.md`](architecture/services.md) — Per-service responsibilities and APIs
- [`kafka-events.md`](architecture/kafka-events.md) — Event schema definitions
- [`database-schema.md`](architecture/database-schema.md) — ERD and table definitions
- [`security-design.md`](architecture/security-design.md) — Auth flow and JWT lifecycle
- [`claim-lifecycle.md`](architecture/claim-lifecycle.md) — Claim state machine
- [`ai-processing-design.md`](architecture/ai-processing-design.md) — AI pipeline design
- [`architecture-decisions.md`](architecture/architecture-decisions.md) — ADRs
- [`sequence-diagrams.md`](architecture/sequence-diagrams.md) — Request flow diagrams
- [`api-contracts.md`](architecture/api-contracts.md) — REST API contract specs

---

## Environment Variables Reference

See [`.env.example`](.env.example) for the full list with descriptions. Required variables:

| Variable              | Description                                       |
|-----------------------|---------------------------------------------------|
| `JWT_SECRET`          | HMAC signing secret — must be ≥ 32 characters    |
| `POSTGRES_AUTH_PASSWORD` | PostgreSQL password for auth_service DB        |
| `POSTGRES_CLAIM_PASSWORD`| PostgreSQL password for claim_service DB       |

All other variables have sensible defaults for local development.

---

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push to `main`/`develop` and all pull requests:

1. **5 parallel jobs** — one per microservice — run `mvn verify`
2. **Docker build validation** — builds each service image after all tests pass
3. Test results uploaded as artifacts (7-day retention)

Maven dependency caching is enabled per service via `actions/setup-java` cache.
