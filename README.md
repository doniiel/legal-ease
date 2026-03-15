# LegalEase

> AI-powered legal document platform for Kazakhstan — helping users create, validate, and understand legal documents with lawyer-authored templates and a multi-stage rule engine.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)

---

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Document Lifecycle](#document-lifecycle)
- [Rule Engine Pipeline](#rule-engine-pipeline)
- [AI Integration](#ai-integration)
- [S3 / MinIO Storage](#s3--minio-storage)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [Full Docker Setup](#full-docker-setup)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Database Migrations](#database-migrations)
- [Team](#team)

---

## Project Overview

LegalEase connects three groups of users:

| Role | What they do |
|------|-------------|
| `ROLE_USER` | Browse published templates, create documents, run rule-engine validation, download PDFs |
| `ROLE_LAWYER` | Create and publish templates with fields and rules; manage validation, risk, matching, conditional, and required-document rules |
| `ROLE_ADMIN` | Approve/reject lawyer applications, manage users and categories, toggle rules globally |

Users who want to become lawyers submit a `LawyerApplication` which an admin reviews. Once approved, the user gains `ROLE_LAWYER`.

---

## Features

- **Template management** — Lawyers define reusable document skeletons with typed fields (`TEXT`, `DATE`, `NUMBER`, etc.)
- **5-type rule engine** — Validation, risk detection, template matching, conditional field requirements, required supporting documents
- **AI layer** — Anthropic Claude powers intent detection, template re-ranking, field suggestions, risk explanation, clause explanation, and final legal review
- **Document versioning** — Every completion creates an immutable PDF version in S3/MinIO
- **Document sharing** — Time-limited public share links using cryptographically secure tokens
- **Audit logging** — Async audit trail for all document lifecycle events
- **JWT auth** — Stateless; 30-min access token, 24-hr refresh token
- **Email verification** — Account activation and password reset via SMTP

---

## Tech Stack

### Backend
| Concern | Technology |
|---------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security (JWT / stateless) |
| Persistence | Spring Data JPA + Hibernate |
| Migrations | Liquibase (`ddl-auto: validate`) |
| DTO mapping | MapStruct |
| Boilerplate | Lombok |
| API docs | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| PDF generation | Apache PDFBox 3.x |
| Object storage | AWS SDK v2 (MinIO / S3-compatible) |
| AI | Anthropic Claude API (via REST) |
| Build | Gradle 9 |

### Frontend
| Concern | Technology |
|---------|-----------|
| Framework | React 19 |
| Language | TypeScript |
| Build tool | Vite |
| State management | Redux Toolkit |
| Routing | React Router v7 |
| UI library | Ant Design + Tailwind CSS |
| HTTP | Axios with JWT interceptors |

### Infrastructure
| Concern | Technology |
|---------|-----------|
| Database | PostgreSQL 16 |
| Object storage | MinIO (S3-compatible) |
| Containerization | Docker + Docker Compose |

---

## System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         Docker Network                           │
│                                                                  │
│  ┌─────────────┐   HTTP    ┌──────────────────┐   JDBC          │
│  │   Frontend   │ ────────▶│     Backend       │──────────┐      │
│  │  (React 19) │           │  (Spring Boot)    │          │      │
│  │  :3000      │           │  :9191            │          ▼      │
│  └─────────────┘           └──────────────────┘   ┌────────────┐│
│                                    │               │ PostgreSQL ││
│                                    │ S3 API        │    :5432   ││
│                                    ▼               └────────────┘│
│                             ┌────────────┐                       │
│                             │   MinIO    │                       │
│                             │ (S3 store) │                       │
│                             │  :9000     │                       │
│                             └────────────┘                       │
└──────────────────────────────────────────────────────────────────┘
         │                          │
         │ SMTP               Anthropic API
         ▼                          ▼
   Gmail / SMTP              Claude (remote)
```

### Backend Package Structure (`kz.legeal.ease.backend`)

```
controller/
  openApi/      — Public/auth endpoints (no auth required)
  user/         — ROLE_USER endpoints
  lawyer/       — ROLE_LAWYER endpoints
  admin/        — ROLE_ADMIN endpoints

service/
  impl/         — Business logic implementations
  rule/
    engine/     — RuleEngine entry point (3 pipelines)
    chain/      — Chain-of-Responsibility: RuleChainBuilder, ChainLink
    handler/    — 11 handlers (Validation, Risk, Matching, Conditional, RequiredDocs, AI*)
    evaluator/  — Low-level condition evaluators (EQUALS, GT, LT, REGEX, …)
    ai/         — Anthropic API integration (RuleAiService)
    context/    — RuleContext (input) + RuleChainContext (mutable state)
    result/     — RuleEngineResult (unified output)
  document/     — Document lifecycle + PDF generation + S3 upload
  audit/        — Async audit logging

domain/         — JPA entities
dto/            — Response DTOs (MapStruct mapped)
request/        — Request DTOs (Jakarta validation)
exception/      — BaseException hierarchy (NotFoundException, BusinessRuleException, …)
mapper/         — MapStruct interfaces
repository/     — Spring Data JPA repositories + Specifications
config/         — Security, S3, Swagger, Async, Mail config
jwt/            — JWT filter, token utilities, PersonDetails
```

---

## Document Lifecycle

```
POST /api/user/documents              → status = DRAFT
         │
         ▼
PUT  /api/user/documents/{id}         → update fields (DRAFT only)
         │
         ▼
GET  /api/user/documents/{id}/suggestions
         │   runs: ConditionalHandler → RequiredDocsHandler
         │         → AIDocsExplainerHandler → AIFieldSuggestionHandler
         ▼
POST /api/user/documents/{id}/complete
         │   runs: ValidationHandler → RiskHandler → RequiredDocsHandler
         │         → AIDocsExplainerHandler → AIRiskExplainerHandler → AIFinalReviewHandler
         │   if valid:
         │     - status = COMPLETED
         │     - PDF generated (PDFBox)
         │     - uploaded to S3 as documents/{userId}/{docId}/v{version}.pdf
         │     - DocumentVersion record created
         ▼
GET  /api/user/documents/{id}/download      → stream PDF bytes
GET  /api/user/documents/{id}/url           → presigned URL (default 60 min)
POST /api/user/documents/{id}/share         → public token (default 48 hr)
GET  /api/user/documents/{id}/versions      → list all immutable versions
GET  /api/user/documents/{id}/versions/{v}/download
```

---

## Rule Engine Pipeline

The rule engine uses Chain-of-Responsibility. Three named pipelines are assembled by `RuleChainBuilder`:

### 1. Matching Chain (`POST /api/user/matching`)
```
AIUnderstandingHandler   ← detects legal intent + extracts entities
       ↓
MatchingHandler          ← scores templates by keyword overlap + conditional boosts
       ↓
AiRankingHandler         ← re-ranks top candidates using Claude
```

### 2. Field Suggestion Chain (`GET /api/user/documents/{id}/suggestions`)
```
ConditionalHandler       ← resolves which fields become required based on current values
       ↓
RequiredDocsHandler      ← identifies mandatory supporting documents
       ↓
AIDocsExplainerHandler   ← generates plain-language explanations for each required doc
       ↓
AIFieldSuggestionHandler ← suggests values for empty fields based on context
```

### 3. Complete Chain (`POST /api/user/documents/{id}/complete`)
```
ValidationHandler        ← enforces all ValidationRules; aborts if any fail
       ↓
RiskHandler              ← evaluates RiskRules; flags warnings (non-blocking)
       ↓
RequiredDocsHandler      ← checks required supporting documents
       ↓
AIDocsExplainerHandler   ← explains required document rationale
       ↓
AIRiskExplainerHandler   ← enriches risk flags with legal context
       ↓
AIFinalReviewHandler     ← overall AI legal review summary
```

---

## AI Integration

All AI calls use the Anthropic Claude API via `RuleAiService`:

| Feature | Endpoint triggered | Claude task |
|---------|-------------------|-------------|
| Intent detection | `POST /api/user/matching` | Classify legal intent, extract entities |
| Template re-ranking | `POST /api/user/matching` | Re-score templates using legal reasoning |
| Field suggestions | `GET /api/user/documents/{id}/suggestions` | Suggest sensible field values |
| Required doc explanation | Both suggestion + complete | Plain-language rationale for each doc |
| Risk explanation | `POST /api/user/documents/{id}/complete` | Elaborate on detected risks |
| Final legal review | `POST /api/user/documents/{id}/complete` | Summary + recommendation |
| Clause explanation | `POST /api/ai/explain-clause` | Explain any legal text in plain language |

---

## S3 / MinIO Storage

PDF files are stored in MinIO (S3-compatible) with the key pattern:

```
documents/{userId}/{documentId}/v{version}.pdf
```

- Each `complete()` call increments `currentVersion` and stores a new immutable object.
- `StorageService` enforces a 20 MB file size limit and PDF-only content type.
- Presigned URLs are generated for direct browser download (default TTL: 60 minutes).
- Share tokens resolve to PDF bytes without requiring authentication.

---

## Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Docker | 24+ |
| Docker Compose | 2.20+ |
| Java | 21+ (local dev only) |
| Node.js | 20+ (local dev only) |

### Local Development

Start only PostgreSQL and MinIO in Docker, run backend and frontend locally.

```bash
# 1. Clone
git clone https://github.com/your-org/legal-ease.git
cd legal-ease

# 2. Copy and edit environment
cp .env.example .env

# 3. Start DB + MinIO only
docker compose up postgres minio -d

# 4. Run backend (hot reload via Spring DevTools)
cd backend
./gradlew bootRun

# 5. Run frontend (hot reload via Vite)
cd ../frontend
npm install
npm run dev
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:9191 |
| Swagger UI | http://localhost:9191/swagger-ui/index.html |
| MinIO console | http://localhost:9001 |

### Full Docker Setup

```bash
# Build and start all services
docker compose up --build -d

# Verify all services are healthy
docker compose ps

# Tail backend logs
docker compose logs -f backend

# Stop everything
docker compose down

# Stop and remove volumes (WARNING: deletes all data)
docker compose down -v

# Open PostgreSQL shell
docker compose exec postgres psql -U postgres -d legal-ease
```

---

## Environment Variables

Create a `.env` file at the repo root (loaded automatically by Docker Compose):

| Variable | Description | Example |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | `legal-ease` |
| `POSTGRES_USER` | DB user | `postgres` |
| `POSTGRES_PASSWORD` | DB password | `strong_password` |
| `POSTGRES_PORT` | Exposed DB port | `5435` |
| `JWT_ACCESS_SECRET` | JWT signing secret (≥32 chars) | `openssl rand -hex 32` |
| `JWT_REFRESH_SECRET` | JWT refresh secret (≥32 chars) | `openssl rand -hex 32` |
| `JWT_ACCESS_EXP_MIN` | Access token TTL (minutes) | `30` |
| `JWT_REFRESH_EXP_MIN` | Refresh token TTL (minutes) | `1440` |
| `MAIL_USERNAME` | SMTP address | `app@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | `xxxx xxxx xxxx xxxx` |
| `BACKEND_PORT` | Backend port | `9191` |
| `FRONTEND_PORT` | Frontend port | `3000` |
| `S3_ENDPOINT` | MinIO/S3 endpoint | `http://minio:9000` |
| `S3_ACCESS_KEY` | MinIO access key | `minioadmin` |
| `S3_SECRET_KEY` | MinIO secret key | `minioadmin` |
| `S3_BUCKET` | Bucket name | `legal-ease-docs` |
| `ANTHROPIC_API_KEY` | Claude API key | `sk-ant-...` |

> **Gmail App Password:** Google Account → Security → 2-Step Verification → App passwords

---

## API Documentation

Swagger UI is served at:
```
http://localhost:9191/swagger-ui/index.html
```

### API Groups

| Tag | Base Path | Role |
|-----|-----------|------|
| Authentication API | `/open-api/auth/*` | Public |
| Public - Categories | `/open-api/categories` | Public |
| Public - Document Sharing | `/api/public/share/*` | Public |
| User - Document Management | `/api/user/documents/*` | USER |
| User - Public Templates | `/api/user/templates/*` | USER |
| User - Template Matching | `/api/user/matching` | USER |
| User - Lawyer Applications | `/api/user/lawyer-applications/*` | USER |
| AI - Legal Assistance | `/api/ai/*` | Authenticated |
| Lawyer - Template Management | `/api/lawyer/templates/*` | LAWYER |
| Lawyer - Validation Rules | `/api/lawyer/validation-rules/*` | LAWYER |
| Lawyer - Risk Rules | `/api/lawyer/risk-rules/*` | LAWYER |
| Lawyer - Matching Rules | `/api/lawyer/matching-rules/*` | LAWYER |
| Lawyer - Conditional Rules | `/api/lawyer/conditional-rules/*` | LAWYER |
| Lawyer - Required Document Rules | `/api/lawyer/required-doc-rules/*` | LAWYER |
| Admin - Category Management | `/api/admin/categories/*` | ADMIN |
| Admin - Lawyer Applications | `/api/admin/lawyer-applications/*` | ADMIN |
| Admin - User Management | `/api/admin/users/*` | ADMIN |
| Admin - Rule Engine Management | `/api/admin/rules/*` | ADMIN |

### Auth Flow

```bash
# 1. Register
POST /open-api/auth/register

# 2. Confirm email
POST /open-api/auth/confirm

# 3. Login → receive access_token + refresh_token
POST /open-api/auth/login

# 4. Use token on all protected endpoints
Authorization: Bearer <access_token>

# 5. Refresh when access token expires
POST /open-api/auth/refresh

# 6. Logout (invalidates refresh token)
POST /open-api/auth/logout
```

---

## Database Migrations

Migrations are managed by **Liquibase** and run automatically on application startup. The schema is declared `ddl-auto: validate` — never `create` or `update`.

```
resources/liquibase/changelogs/v1/
├── 001-create-sequences.xml
├── 002-create-role.xml
├── 003-create-users.xml
├── 004-create-user-role.xml
├── 005-create-verification-code.xml
├── 006-create-refresh-tokens.xml
├── 007-create-lawyer-application.xml
├── 008-insert-default-roles.xml        ← seeds: USER, LAWYER, ADMIN
├── 009-insert-default-admins.xml       ← seeds: default admin accounts
├── 010-create-category.xml
├── 011-create-template.xml
├── 012-create-template-field.xml
├── 013-create-document.xml
├── 014-create-document-field-value.xml
├── 015-create-validation-rule.xml
├── 016-create-risk-rule.xml
├── 017-create-matching-rule.xml
├── 018-create-conditional-rule.xml
├── 019-create-required-doc-rule.xml
├── 020-create-document-versions.xml
├── 021-add-current-version-to-document.xml
├── 022-create-document-shares.xml
└── 023-create-audit-logs.xml
```

**Rules:**
- Never modify existing changesets
- New files: `NNN-description.xml` (increment N)
- Include a `<rollback>` block for every change
- One logical change per file

---

## Team

| Name | Role | Contact |
|------|------|---------|
| Daniyal | Backend Developer | orynbekdanial8@gmail.com |
| Yegazy | Backend Developer | yergazy.abdullayev@gmail.com |
| Olzhas | Backend Developer | olzhasergali56@gmail.com |

---

<div align="center">
  Built for legal accessibility in Kazakhstan
</div>
