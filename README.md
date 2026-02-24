# ⚖️ LegalEase

> AI-powered legal document platform — helping users understand, create and validate legal documents in Kazakhstan.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Local Development](#local-development)
    - [Full Docker Setup](#full-docker-setup)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Database Migrations](#database-migrations)
- [Contributing](#contributing)

---

## Overview

LegalEase is a platform that bridges the gap between complex legal documents and everyday users. Lawyers create and validate document templates; users generate, analyze and understand their legal documents through an AI-powered interface.

**Key roles:**
| Role | Capabilities |
|------|-------------|
| `USER` | Create documents, view analysis, submit lawyer application |
| `LAWYER` | Create templates, define validation & risk rules |
| `ADMIN` | Approve lawyer applications, manage users |

---

## Architecture

```
┌─────────────┐     HTTP      ┌─────────────────┐     JDBC      ┌──────────────┐
│   Frontend  │ ────────────▶ │     Backend      │ ────────────▶ │  PostgreSQL  │
│  (Next.js)  │               │  (Spring Boot)   │               │              │
└─────────────┘               └─────────────────┘               └──────────────┘
       │                               │
       │                               │ SMTP
       │                        ┌──────▼──────┐
       │                        │  Gmail SMTP  │
       └────────────────────────│   (Email)    │
                          legalease-network
```

All services communicate over a shared Docker bridge network `legalease-network`.

---

## Tech Stack

**Backend**
- Java 21 + Spring Boot 3
- Spring Security + JWT (stateless)
- Spring Data JPA + Hibernate
- Liquibase (database migrations)
- Thymeleaf (email templates)
- MapStruct (DTO mapping)
- Lombok

**Frontend**
- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS

**Infrastructure**
- PostgreSQL 16
- Docker + Docker Compose
- GitHub Actions (CI/CD)

---

## Project Structure

```
legal-ease/
├── backend/
│   ├── src/
│   ├── build.gradle
│   └── Dockerfile
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── data/               ← Docker volumes (git-ignored)
│   └── postgres/
├── docker-compose.yml
├── .env                ← единый env файл
└── README.md
```

---

## Getting Started

### Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Docker | 24+ | [docs.docker.com](https://docs.docker.com/get-docker/) |
| Docker Compose | 2.20+ | Included with Docker Desktop |
| Java | 21+ | [adoptium.net](https://adoptium.net/) (local dev only) |
| Node.js | 20+ | [nodejs.org](https://nodejs.org/) (local dev only) |

---

### Local Development

> Best for active development — hot reload for both backend and frontend.
> Only PostgreSQL runs in Docker.

**1. Clone the repository**
```bash
git clone https://github.com/your-org/legal-ease.git
cd legal-ease
```

**2. Start only the database**
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up postgres -d
```

PostgreSQL will be available at `localhost:5435`.

**3. Run the backend**
```bash
cd backend
./mvnw spring-boot:run
```

Backend starts at `http://localhost:9191`
Swagger UI: `http://localhost:9191/swagger-ui/index.html`

**4. Run the frontend**
```bash
cd frontend
npm install
npm run dev
```

Frontend starts at `http://localhost:3000`

---

### Full Docker Setup

> Runs everything in Docker — closest to production.

**1. Clone and configure**
```bash
git clone https://github.com/your-org/legal-ease.git
cd legal-ease
```

**2. Build and start all services**
```bash
docker compose up --build -d
```

**3. Check services are healthy**
```bash
docker compose ps
```

Expected output:
```
NAME                   STATUS
legalease-postgres     healthy
legalease-backend      healthy
legalease-frontend     healthy
```

**5. Access the application**

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:9191 |
| Swagger UI | http://localhost:9191/swagger-ui/index.html |

---

### Useful Commands

```bash
# View logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres

# Restart a single service
docker compose restart backend

# Stop everything
docker compose down

# Stop and remove volumes (WARNING: deletes all data)
docker compose down -v

# Rebuild after code changes
docker compose up --build backend -d

# Open PostgreSQL shell
docker compose exec postgres psql -U postgres -d legal-ease
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values.

| Variable | Description | Example |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | `legal-ease` |
| `POSTGRES_USER` | Database user | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `strong_password` |
| `POSTGRES_PORT` | Exposed DB port | `5435` |
| `JWT_ACCESS_SECRET` | JWT signing key (min 32 chars) | `openssl rand -hex 32` |
| `JWT_REFRESH_SECRET` | JWT refresh key (min 32 chars) | `openssl rand -hex 32` |
| `JWT_ACCESS_EXP_MIN` | Access token TTL in minutes | `30` |
| `JWT_REFRESH_EXP_MIN` | Refresh token TTL in minutes | `1440` |
| `MAIL_USERNAME` | Gmail address | `app@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | `xxxx xxxx xxxx xxxx` |
| `BACKEND_PORT` | Backend exposed port | `9191` |
| `FRONTEND_PORT` | Frontend exposed port | `3000` |

> **Gmail App Password:** Go to Google Account → Security → 2-Step Verification → App passwords

---

## API Documentation

Swagger UI is available when the backend is running:

```
http://localhost:9191/swagger-ui/index.html
```

### Authentication flow

```
POST /open-api/auth/register        # Register new user
POST /open-api/auth/confirm         # Confirm email with code
POST /open-api/auth/login           # Login → get access + refresh tokens
POST /open-api/auth/refresh         # Refresh access token
POST /open-api/auth/reset-password  # Send reset code
POST /open-api/auth/change-password # Change password with code
POST /open-api/auth/logout          # Revoke refresh token
```

All protected endpoints require:
```
Authorization: Bearer <access_token>
```

---

## Database Migrations

Migrations are managed by **Liquibase** and run automatically on startup.

```
resources/liquibase/
├── master.xml
└── changelogs/
    └── v1/
        ├── 001-create-sequences.xml
        ├── 002-create-role.xml
        ├── 003-create-users.xml
        ├── 004-create-user-role.xml
        ├── 005-create-verification-code.xml
        ├── 006-create-refresh-tokens.xml
        ├── 007-create-lawyer-application.xml
        ├── 008-insert-default-roles.xml      # Seeds: USER, LAWYER, ADMIN roles
        └── 009-insert-default-admins.xml     # Seeds: default admin accounts
```

**Rules for adding migrations:**
- Never modify existing changesets
- New file naming: `YYYY-MM-DD-NNN-description.xml`
- Always include a `<rollback>` block
- One logical change per file

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

**Commit message convention:** [Conventional Commits](https://www.conventionalcommits.org/)
```
feat:     new feature
fix:      bug fix
docs:     documentation only
refactor: code refactoring
test:     adding tests
chore:    build / config changes
```

---

## Team

| Name | Role | Contact |
|------|------|--|
| Daniyal | Backend Developer | orynbekdanial8@gmail.com |
| Yegazy | Backend Developer | yergazy.abdullayev@gmail.com |
| Olzhas | Backend Developer | olzhasergali56@gmail.com |

---

<div align="center">
  <sub>Built with ❤️ for legal accessibility in Kazakhstan</sub>
</div>