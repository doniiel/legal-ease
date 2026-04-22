# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LegalEase is an AI-powered legal document platform for Kazakhstan. Users can create/validate legal documents, lawyers create templates and define rules, and admins manage the platform.

## Commands

### Backend (Java 21 / Spring Boot 3 / Gradle)
```bash
cd backend
./gradlew clean build        # Build JAR
./gradlew test               # Run all tests
./gradlew test --tests "kz.legeal.ease.backend.SomeTest"  # Run single test
./gradlew bootRun            # Run locally (requires DB)
```

### Frontend (React 19 / TypeScript / Vite)
```bash
cd frontend
npm install
npm run dev      # Dev server on :3000
npm run build    # TypeScript check + Vite build
npm run lint     # ESLint
```

### Full Stack (Docker)
```bash
docker compose up --build -d           # Start all services
docker compose up -d postgres          # Start DB only (for local dev)
docker compose logs -f backend         # Tail logs
docker compose down                    # Stop all
```

## Architecture

### Stack
- **Backend:** Spring Boot 3.5.5, Spring Security (JWT/stateless), Spring Data JPA, Liquibase, MapStruct, Lombok
- **Frontend:** React 19, Redux Toolkit, React Router v7, Ant Design, Tailwind CSS
- **Database:** PostgreSQL 16 (schema managed entirely by Liquibase, `ddl-auto: validate`)
- **Auth:** JWT — 30-min access token, 24-hr refresh token; no sessions

### Backend Package Structure (`kz.legeal.ease.backend`)
- **`controller/`** — Split by role: `openApi/` (public/auth), `user/`, `lawyer/`, `admin/`
- **`service/`** — Business logic with `impl/` for implementations
- **`service/rule/`** — Rule engine subsystem: `evaluator/`, `ai/` (Anthropic API), `chain/`, `context/`
- **`service/document/`** — Document generation from templates
- **`domain/`** — JPA entities
- **`dto/`** — Request/response DTOs (mapped via MapStruct in `mapper/`)
- **`jwt/`** — JWT filter, token utilities, `PersonDetails` (Spring `UserDetails` impl)
- **`repository/specification/`** — JPA Specifications for dynamic queries

### API Route Conventions
- `POST /open-api/auth/*` — Public auth endpoints (login, register, verify, refresh)
- `GET /open-api/categories` — Public categories
- `/api/users/*` — Requires `ROLE_USER`
- `/api/lawyers/*` — Requires `ROLE_LAWYER`
- `/api/admin/*` — Requires `ROLE_ADMIN`

### Frontend Structure (`src/`)
- **`app/`** — App root, router setup, Redux store config
- **`pages/`** — Route-level components (`pages/old/` is excluded from TypeScript build)
- **`features/`** — Feature modules with Redux slices + API calls (e.g., `features/auth/`)
- **`widgets/`** — Reusable UI blocks (`widgets/auth/` excluded from TS build)
- **`shared/api/`** — Axios instance with JWT interceptors; base URL via `VITE_API_URL`

### Key Domain Concepts
- **Template** — Lawyer-created document skeleton with `TemplateField`s
- **Document** — User-generated instance of a template with `DocumentFieldValue`s
- **Rules** — Three types lawyers define: `ValidationRule`, `RiskRule`, `MatchingRule`, plus complex `ConditionRule`
- **LawyerApplication** — Workflow for users to become lawyers (admin approval required)

### Database
Liquibase migrations live in `backend/src/main/resources/liquibase/changelogs/v1/` (20 files). Schema changes must go through new Liquibase changesets — never use `ddl-auto: create/update`.

### Environment
All secrets come from `.env` at repo root (loaded by Docker Compose). Key variables: `POSTGRES_*`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `MAIL_*`, `BACKEND_PORT=9191`, `FRONTEND_PORT=3000`.
