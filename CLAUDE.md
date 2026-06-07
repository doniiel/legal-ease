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

---

## Frontend Structure (`frontend/src/`)

Follows **Feature-Sliced Design (FSD)**. Import only through `index.ts` public APIs.

```
src/
├── app/
│   ├── App.tsx
│   ├── main.tsx
│   ├── index.css / styles/theme.css
│   ├── router/
│   │   ├── appRouter.tsx       # <RouterProvider> setup
│   │   └── router.ts           # route definitions
│   └── store/
│       └── index.ts            # Redux store config
│
├── pages/                      # Route-level components, each is index.tsx
│   ├── login/                  # Public
│   ├── register/
│   ├── confirm/
│   ├── forgot-password/
│   ├── document-verify/        # Public share verification
│   ├── dashboard-home/         # ROLE_USER
│   ├── documents/
│   ├── document-detail/
│   ├── profile/
│   ├── settings/
│   ├── matching/
│   ├── lawyer-application/
│   ├── lawyer-templates/       # ROLE_LAWYER
│   ├── lawyer-rule-manager/
│   ├── lawyer-matching-rules/
│   ├── lawyer-clauses/
│   ├── lawyer-documents/
│   ├── admin-dashboard/        # ROLE_ADMIN
│   ├── admin-users/
│   ├── admin-applications/
│   ├── admin-categories/
│   ├── admin-audit/
│   ├── admin-rules/
│   └── old/                    # excluded from TS build (legacy)
│
├── widgets/                    # Self-contained UI blocks, no business logic
│   ├── layout/ui/MainLayout.tsx        # App shell (Sider + Header + Content)
│   ├── home/ui/DashboardHomePanel.tsx
│   ├── documents/ui/
│   │   ├── DocumentsListPanel.tsx
│   │   └── DocumentDetailPanel.tsx
│   ├── lawyer/ui/
│   │   ├── LawyerTemplatesPanel.tsx
│   │   ├── LawyerRuleManagerPanel.tsx
│   │   ├── LawyerMatchingRulesPanel.tsx
│   │   ├── LawyerClausesPanel.tsx
│   │   └── LawyerDocumentsPanel.tsx
│   ├── admin/ui/
│   │   ├── AdminDashboardPanel.tsx
│   │   ├── AdminUsersPanel.tsx
│   │   ├── AdminLawyerApplicationsPanel.tsx
│   │   ├── AdminCategoriesPanel.tsx
│   │   ├── AdminRulesPanel.tsx
│   │   └── AdminAuditPanel.tsx
│   ├── lawyer-application/ui/LawyerApplicationPanel.tsx
│   ├── matching/ui/MatchingPanel.tsx
│   ├── profile/ui/ProfilePanel.tsx
│   ├── settings/ui/SettingsPanel.tsx
│   └── auth/                   # excluded from TS build (legacy)
│
├── features/                   # User scenarios — Redux slices + API hooks
│   ├── auth/
│   │   ├── api/auth-api.ts
│   │   ├── model/auth-slice.ts
│   │   ├── model/use-auth.ts
│   │   └── index.ts
│   ├── documents/api/
│   │   ├── document-api.ts
│   │   └── document-verification-api.ts
│   ├── templates/api/user-template-api.ts
│   ├── categories/api/public-category-api.ts
│   ├── profile/
│   │   ├── api/profile-api.ts
│   │   └── model/use-profile.ts
│   ├── lawyer/
│   │   ├── api/                # lawyer-template, validation/risk/matching/conditional/required-doc rules, clauses, documents, reviews
│   │   └── model/              # use-lawyer-templates, use-lawyer-documents, use-matching-rules
│   ├── lawyer-application/api/lawyer-application-api.ts
│   ├── matching/api/matching-api.ts
│   ├── share/api/public-share-api.ts
│   ├── ai/api/ai-api.ts
│   └── admin/
│       ├── api/                # admin-user, admin-lawyer, admin-category, admin-audit, admin-metrics, admin-rules
│       ├── lib/user-role.ts
│       ├── model/              # use-admin-users, use-admin-applications, use-admin-categories, use-admin-audit, use-rules
│       └── ui/                 # BlockConfirmModal, UserDetailDrawer
│
└── shared/
    ├── api/
    │   ├── base.ts             # Axios instance with JWT interceptors + refresh logic
    │   └── index.ts
    ├── auth/constants.ts       # Token storage keys
    ├── hooks/use-is-mobile.ts
    ├── container/index.tsx
    └── ui/
        ├── index.ts
        ├── ProtectedRoute.tsx  # Role-based route guard
        ├── StatCard.tsx
        ├── table-components.tsx
        └── container/index.tsx
```

---

## Backend Structure (`kz.legeal.ease.backend`)

```
backend/src/main/java/kz/legeal/ease/backend/
├── config/               # Spring config beans
│   ├── SecurityConfig.java       # JWT filter, CORS, role-based routes
│   ├── JwtProperties.java
│   ├── S3Config.java / S3Properties.java
│   ├── MailConfig.java
│   ├── AsyncConfig.java
│   ├── CacheConfig.java
│   ├── RestTemplateConfig.java
│   └── SwaggerConfig.java
│
├── controller/
│   ├── openApi/                  # Public (no auth)
│   │   ├── AuthController.java
│   │   ├── PublicCategoryController.java
│   │   ├── PublicShareController.java
│   │   └── DocumentVerificationController.java
│   ├── TemplateController.java   # ROLE_LAWYER — template CRUD
│   ├── MyTemplateController.java # ROLE_LAWYER — my templates
│   ├── RuleController.java       # ROLE_LAWYER — validation rules
│   ├── ValidationRuleController.java
│   ├── RiskRuleController.java
│   ├── MatchingRuleController.java
│   ├── ConditionalRuleController.java
│   ├── RequiredDocRuleController.java
│   ├── ClauseController.java
│   ├── LawyerDocumentController.java
│   ├── DocumentController.java   # ROLE_USER — document operations
│   ├── LawyerApplicationController.java
│   ├── MatchingController.java
│   ├── ReviewController.java
│   ├── ProfileController.java
│   ├── UserController.java       # ROLE_ADMIN
│   ├── AiController.java
│   ├── AuditController.java
│   ├── CategoryController.java
│   └── MetricsController.java
│
├── service/
│   ├── rule/                     # Rule engine subsystem
│   │   ├── engine/RuleEngine.java
│   │   ├── handler/              # Chain-of-responsibility handlers
│   │   │   ├── ValidationHandler, RiskHandler, MatchingHandler
│   │   │   ├── ConditionalHandler, RequiredDocsHandler
│   │   │   └── AI*Handler (field suggestions, ranking, review, explanations)
│   │   ├── chain/                # RuleChainBuilder, ChainLink, RuleChainContext
│   │   ├── evaluator/RuleConditionEvaluator.java
│   │   ├── context/RuleContext.java
│   │   ├── result/               # RuleEngineResult + typed sub-results
│   │   ├── common/               # Value objects: RiskItem, ValidationError, etc.
│   │   ├── ai/RuleAiService.java + impl/DeepSeekRuleAIService.java
│   │   └── RuleHandler.java      # Handler interface
│   ├── tempate/                  # Template services (note: typo in package name)
│   │   ├── TemplateCommandService + impl
│   │   ├── TemplateQueryService + impl
│   │   └── TemplatePublicQueryService + impl
│   ├── lawyer/
│   │   ├── LawyerDocumentService + impl
│   │   └── LegalClauseService + impl
│   ├── document/                 # Document generation from templates
│   ├── impl/                     # Implementations for top-level services
│   ├── UserService, UserProfileService, UserRoleService
│   ├── ValidationRuleService, RiskRuleService, MatchingRuleService
│   ├── RequiredDocRuleService, ConditionalRuleService (via RuleController)
│   ├── TokenService, VerificationService
│   ├── NotificationService, RateLimitService
│   └── MatchingService
│
├── domain/               # JPA entities
│   ├── User, Role, UserRole
│   ├── Template, TemplateField
│   ├── Document, DocumentFieldValue, DocumentVersion, DocumentShare, DocumentReview
│   ├── ValidationRule, RiskRule, MatchingRule, ConditionRule, RequiredDocRule
│   ├── LegalClause
│   ├── LawyerApplication
│   ├── RefreshToken, VerificationCode
│   ├── AuditLog
│   └── AbstractAuditingEntity
│
├── dto/                  # Request/response DTOs
│   ├── document/         # DocumentDto, DocumentPreviewDto, DocumentFieldValueDto, etc.
│   ├── template/         # TemplateDto, TemplatePreviewDto, TemplateFieldDto
│   ├── ai/               # ClauseExplainResponse, DocumentExplainResponse
│   └── (flat)            # Auth, Category, User, Rule, etc. DTOs
│
├── mapper/               # MapStruct mappers (entity ↔ DTO)
├── repository/
│   └── specification/    # JPA Specifications for dynamic queries
├── jwt/                  # JwtFilter, JwtUtils, PersonDetails
├── storage/              # StorageService interface + S3StorageService impl
├── enums/                # DocumentStatus, AuditAction, Gender, etc.
└── util/                 # SecurityUtils, CodeUtils
```

---

## API Route Conventions

| Prefix | Auth | Description |
|---|---|---|
| `POST /open-api/auth/*` | None | Login, register, verify email, refresh token |
| `GET /open-api/categories` | None | Public category list |
| `GET /open-api/share/*` | None | Public document share |
| `/api/users/*` | `ROLE_USER` | Document creation, profile, matching |
| `/api/lawyers/*` | `ROLE_LAWYER` | Template/rule management |
| `/api/admin/*` | `ROLE_ADMIN` | User/application/category management |

---

## Key Domain Concepts

- **Template** — Lawyer-created document skeleton with `TemplateField`s (typed input fields)
- **Document** — User-generated instance of a template; stores `DocumentFieldValue`s per field
- **Rules** — Four types lawyers define per template:
  - `ValidationRule` — field-level validation (regex, range, required)
  - `RiskRule` — flags risky field value combinations
  - `MatchingRule` — criteria for template recommendation
  - `ConditionRule` — complex conditional logic
  - `RequiredDocRule` — lists documents user must attach
- **RuleEngine** — Chain-of-responsibility pipeline; runs handlers in sequence, builds `RuleEngineResult`
- **LawyerApplication** — User submits → admin approves → user gains `ROLE_LAWYER`
- **LegalClause** — Reusable clause snippets lawyers can embed in templates
- **DocumentShare** — Shareable link (public token) for a finalized document
- **AuditLog** — Tracks admin actions on users/applications

---

## Database

Liquibase migrations: `backend/src/main/resources/liquibase/changelogs/v1/` (20 changesets).  
Schema changes **must** go through new Liquibase changesets — never use `ddl-auto: create/update`.

---

## Environment

Secrets come from `.env` at repo root (loaded by Docker Compose).  
Key variables: `POSTGRES_*`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `MAIL_*`, `BACKEND_PORT=9191`, `FRONTEND_PORT=3000`, `S3_*`.
