# LegalEase — Project Context Snapshot

> Last updated: 2026-03-28
> Purpose: Persistent reference for Claude Code sessions. Read this at the start of any session to rebuild full project context.

---

## 1. Project Overview

**Name:** LegalEase Backend
**Repository root:** `/home/daniyal/Documents/projects/legal-ease/`
**Working directory for backend:** `backend/`

### Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security (JWT, stateless) |
| Database | PostgreSQL 16 |
| Schema migrations | Liquibase (`ddl-auto: validate`) |
| ORM | Spring Data JPA + Hibernate |
| PDF generation | Apache PDFBox 3.0.3 |
| QR codes | ZXing 3.5.3 (`core` + `javase`) |
| Object storage | AWS SDK v2 → MinIO (S3-compatible) |
| AI rules engine | Anthropic Claude API (`@Profile("!test")`) |
| Mapping | MapStruct 1.5.5 |
| Build | Gradle (Java 21 source + target) |
| Frontend | React 19 + TypeScript + Vite + Ant Design |

### Architecture Summary

```
[Client] → JWT filter → Controllers (openApi / user / lawyer / admin)
                       → Services (business logic)
                       → Repository (JPA / Liquibase schema)
                       → Storage (MinIO / S3)
```

**Route namespaces:**
- `POST /open-api/auth/*` — public auth (login, register, confirm, refresh, reset, change-password, logout)
- `GET  /open-api/categories` — public category list
- `GET  /open-api/documents/verify/{id}` — public document verification (QR destination)
- `/api/users/*` — requires `ROLE_USER`
- `/api/lawyers/*` — requires `ROLE_LAWYER`
- `/api/admin/*` — requires `ROLE_ADMIN`
- `/api/ai/*` — requires any authenticated user

**Backend package root:** `kz.legeal.ease.backend`

Key sub-packages:
- `controller/` — split by role: `openApi/`, `user/`, `lawyer/`, `admin/`
- `service/` — business logic with `impl/` sub-packages
- `service/rule/` — rule engine: `evaluator/`, `ai/`, `chain/`, `context/`
- `service/document/` — PDF generation + verification
- `domain/` — JPA entities
- `dto/` — request/response DTOs (mapped via MapStruct in `mapper/`)
- `jwt/` — `JwtUtils`, `JwtFilter`, `PersonDetails`
- `storage/` — `StorageService` interface + `S3StorageService` impl
- `repository/` — Spring Data repositories + JPA Specifications

---

## 2. Implemented Features

### 2.1 Template System

- **Entity:** `Template` — has `title`, `description`, `body` (TEXT, nullable), `category`, `status` (DRAFT/PUBLISHED), `lawyer` (owner), `templateFields`
- **Body field:** nullable `TEXT` column added via Liquibase changeset `2026-03-28-001-add-template-body.xml`
- **Placeholder syntax:** `{{field_key}}` — double-curly-brace, snake_case field key
- **Renderer:** `TemplateBodyRenderer.render(String body, Map<String,String> fieldValues)`
  - Regex: `\{\{\s*([^}\s][^}]*)\s*\}\}`
  - Missing keys replaced with `[key]` (not blank, not error)
  - Returns `extractPlaceholders(String body)` → `LinkedHashSet<String>` (order-preserving)

### 2.2 Narrative PDF Generation

**Class:** `DocumentPdfServiceImpl` (implements `DocumentPdfService`)
**Injected:** `TemplateBodyRenderer`
**`@Value` field:** `${app.base-url:https://legalease.kz}` — has Java default so tests work without Spring

**Two rendering modes:**
1. **Narrative mode** — template has a `body` → renders text with placeholder substitution
2. **Legacy mode** — template has no `body` → renders a field label → value table (backward compatibility)

**Render pipeline (sealed interface `RenderItem`):**
```
ParagraphItem     → body paragraph text
SectionCardItem   → section header card
FieldRowItem      → legacy field table row
SealItem          → verification QR block
```

**Fonts (Unicode/Cyrillic support):**
- `FreeSans.ttf` → regular body text
- `FreeSansBold.ttf` → headings / labels
- Both embedded as `PDType0Font` (CID TrueType Identity-H)
- Located at `backend/src/main/resources/fonts/`
- Verified with `pdffonts`: `CID TrueType  Identity-H  yes yes yes`

### 2.3 QR Code

- **Library:** ZXing `QRCodeWriter` + `MatrixToImageWriter`
- **Content:** `${app.base-url}/documents/verify/{documentId}` — URL of verification endpoint
- **Error correction:** `ErrorCorrectionLevel.M`, margin 1
- **Size in PDF:** 80×80 pt, embedded via `LosslessFactory.createFromImage`
- **Round-trip verified:** encode then decode produces identical URL

### 2.4 SHA-256 Document Hash

- **When:** computed after PDF bytes are generated in `DocumentServiceImpl.complete()`
- **What:** `HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pdfBytes))`
- **Stored:** `Document.contentHash` — `VARCHAR(64)` column, nullable
- **Liquibase:** `2026-03-28-002-add-content-hash-to-documents.xml`
- **Use:** presence of `contentHash != null` → document is `VALID` in verification response

### 2.5 Public Verification Endpoint

**Route:** `GET /open-api/documents/verify/{id}` — no authentication required
**Controller:** `DocumentVerificationController` (in `controller/openApi/`)
**Service:** `DocumentVerificationServiceImpl`

**Logic:**
1. Document not found → `INVALID`, no details
2. Document soft-deleted → `INVALID`
3. `contentHash == null` → `INVALID` (not properly completed)
4. `contentHash` present → `VALID`

**Response DTO:** `DocumentVerificationResponse`
```json
{
  "status": "VALID",
  "documentId": "DOC-000042",
  "createdAt": "2026-03-28",
  "createdBy": "Full Name or email",
  "hashValid": true
}
```
- `documentId` formatted as `"DOC-" + String.format("%06d", id)`
- `@JsonInclude(NON_NULL)` — `createdAt` / `createdBy` absent on INVALID responses
- `createdBy` resolution: `user.fio` → `createdBy` email → `"—"`

### 2.6 Document PDF Storage (MinIO/S3)

- **Service:** `StorageService` interface → `S3StorageService` (AWS SDK v2)
- **Config:** `storage.s3.*` properties (endpoint, bucket, region, access-key, secret-key)
- **Bucket init:** `S3Config` auto-creates the bucket on startup if not present
- **Download:** `GET /api/user/documents/{id}/download` → returns `application/pdf` bytes
- **Presigned URL:** `GET /api/user/documents/{id}/url` → returns time-limited S3 URL
- **Versioning:** `Document.currentVersion` int counter, versions stored as separate S3 objects

### 2.7 Document Sharing

- **Route:** `POST /api/user/documents/{id}/share` → creates `DocumentShare` with token + expiry
- **Public access:** `GET /api/public/share/{token}` → returns document without auth
- **Config:** `app.share.expiry-hours`, `app.share.cleanup-interval-hours`

---

## 3. PDF Structure (Final Version)

```
┌─────────────────────────────────────────────────────────┐
│  HEADER                                                   │
│  "LegalEase"  (bold, 18pt)       DOC-000042              │
│  "Юридический документ"          28.03.2026              │
├─────────────────────────────────────────────────────────┤
│  TITLE BLOCK                                             │
│  "Договор купли-продажи №42"  (bold, 16pt, centered)     │
│  ─────────────────────────────────────────────────────  │
│                                                           │
│  NARRATIVE BODY  (regular 11pt, justified, line-wrapped)  │
│  "Настоящий договор заключён 28.03.2026 между Daniyal…"  │
│  (placeholders substituted from field values)            │
│                                                           │
│  ┌ VERIFICATION SEAL ─────────────────────────────────┐  │
│  │  [QR CODE 80×80pt]   Документ создан через         │  │
│  │                      платформу LegalEase.           │  │
│  │                      Отсканируйте QR-код для        │  │
│  │                      проверки подлинности.          │  │
│  └─────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│  FOOTER  (centered, 9pt)                                 │
│  "Страница 1 из 1"                                       │
└─────────────────────────────────────────────────────────┘

WATERMARK: diagonal "LegalEase" at 6% alpha, 33° rotation
```

**Key layout constants (in `DocumentPdfServiceImpl`):**
- Page: A4 (595 × 842 pt), margins 50pt
- Header height: ~50pt
- Seal height: `SEAL_H = 163f` (total), `SEAL_CONTENT_H = 114f`
- QR size: `QR_SIZE = 80f`
- Footer: page number only — `"Страница N из N"`, centered
- Fonts: `FreeSans` (body), `FreeSansBold` (headings)

**What was removed (by design decision):**
- ❌ "ДЕТАЛИ ДОКУМЕНТА" field table in narrative mode
- ❌ Raw verification URL in footer
- ❌ Metadata columns in seal section

---

## 4. Business Logic Flow

```
Lawyer creates Template
  └── defines TemplateFields (key, label, type, required, order)
  └── optionally writes body with {{field_key}} placeholders
  └── publishes template (DRAFT → PUBLISHED)

User browses published templates
  └── selects template, fills in FieldValues
  └── creates Document (status: DRAFT)

Document lifecycle:
  DRAFT → [validate] → VALIDATED → [complete] → COMPLETED → [archive] → ARCHIVED
    │                                   │
    │                                   └── PDF generated by DocumentPdfServiceImpl
    └── [delete]                             └── SHA-256 hash stored in Document.contentHash
                                             └── PDF uploaded to MinIO (S3)

PDF generation detail:
  Document + Template.body → TemplateBodyRenderer.render()
    → replace {{field_key}} with FieldValues
    → produces narrative text
  DocumentPdfServiceImpl.generate(document)
    → buildRenderQueue() → list of RenderItems
    → drawPage(items) → PDFBox operations
    → drawWatermarkStamp() → diagonal "LegalEase" alpha overlay
    → returns byte[]
  DocumentServiceImpl.complete()
    → calls pdfService.generate(doc)
    → doc.setContentHash(sha256Hex(pdfBytes))
    → storageService.uploadFile(...)
    → saves document
```

---

## 5. Verification System

```
PDF is generated
  └── QR code encodes: https://legalease.kz/documents/verify/{id}
  └── SHA-256 of PDF bytes stored in documents.content_hash

User scans QR code
  └── Browser opens GET /open-api/documents/verify/{id}
  └── No authentication required (public endpoint)

Verification logic (DocumentVerificationServiceImpl):
  - Document not found   → { status: "INVALID", hashValid: false }
  - Document deleted     → { status: "INVALID", hashValid: false }
  - contentHash is null  → { status: "INVALID", hashValid: false }
  - contentHash present  → { status: "VALID",   hashValid: true,
                             documentId: "DOC-XXXXXX",
                             createdAt: "yyyy-MM-dd",
                             createdBy: "Full Name" }
```

---

## 6. Known Issues / TODO

### CRITICAL
- **CORS wildcard + credentials** (`SecurityConfig` allows all origins with `allowCredentials = true`) — must restrict origins in production
- **IIN in `UserProfileDto`** — IIN (tax ID) is a sensitive identifier and should not be returned in profile responses
- **No auth rate limiting** — `/open-api/auth/login` and `/open-api/auth/register` have no brute-force protection

### HIGH
- **In-memory rate limiter** (`RateLimitService`) — `ConcurrentHashMap`-based, not distributed. Resets on restart. Should be replaced with Redis/Bucket4j for multi-instance deployments
- **Missing pagination** on rule list endpoints (`/api/lawyer/matching-rules`, `/api/admin/rules/**`) — returns unbounded lists
- **Review delete ownership** — `DELETE /api/lawyer/reviews/{id}` should verify the reviewing lawyer owns the review, not just that they are a lawyer
- **HTTP 200 instead of 201** on 5 rule creation controllers (should return `201 Created` with `Location` header)

### MEDIUM
- `LawyerRequestSearchCriteria` is not annotated with `@Valid` in the admin controller
- `fieldValues` map in `CreateDocumentRequest` has no size constraints
- Missing `@Valid` on some nested request objects

### LOW
- `/open-api/categories` response not cached (Caffeine is configured but not applied here)
- No `ETag` or `Last-Modified` headers on document download

### TESTING TODO
- Full API test coverage is implemented (see Section 7) — needs first full clean run to confirm all pass
- Load / performance tests not written
- Frontend E2E tests (Playwright/Cypress) not written

---

## 7. Testing State

### PDF Generation Tests (`PdfGenerationTest.java`)
- ✅ `generateNarrativePdf()` — full narrative mode with Cyrillic text
- ✅ `generateLegacyFallbackPdf()` — field-table mode (no template body)
- ✅ `%PDF` magic bytes assertion
- ✅ SHA-256 hex output printed to console
- ✅ Saves `test-document.pdf` and `test-document-legacy.pdf` to `backend/`

### Font Verification
- ✅ `pdffonts`: `FreeSans`, `FreeSansBold` embedded as `CID TrueType Identity-H yes yes yes`
- ✅ `pdftotext`: all Cyrillic text extracted correctly
- ✅ `pdfimages`: 1 image (the QR code) per document

### QR Verification
- ✅ ZXing round-trip test: encode URL → decode → identical string
- ✅ Decoded URL: `https://legalease.kz/documents/verify/42`

### API Integration Tests (`kz.legeal.ease.backend.api.*`)
All tests use: `@SpringBootTest` + `@AutoConfigureMockMvc` + Testcontainers PostgreSQL (singleton pattern) + `@MockitoBean` for `StorageService` + `EmailSenderService` + `@Profile("test")` `StubAiService`

| Test Class | Coverage |
|---|---|
| `AuthApiTest` | Login, register, confirm, reset, change-password, refresh, logout — happy + negative |
| `PublicApiTest` | Categories list, document verification (VALID/INVALID/format) |
| `AdminApiTest` | Category CRUD, user management, lawyer applications, audit logs, rule toggles, metrics |
| `LawyerApiTest` | Template CRUD + publish, matching rules, document reviews, lawyer documents |
| `UserDocumentApiTest` | Full document lifecycle, profile, lawyer application, matching |
| `SecurityApiTest` | Missing token → 401, invalid JWT → 401, IDOR (user2 cannot access user1's docs), role isolation (all 3 roles) |

**Test infrastructure files:**
- `src/test/resources/application-test.yml` — overrides DB URL (Testcontainers), high rate limits, stub mail
- `src/test/java/.../stub/StubAiService.java` — `@Profile("test")` no-op AI service
- `src/test/java/.../api/BaseIntegrationTest.java` — base class with singleton container, user factory, token generator, MockMvc helpers

---

## 8. Important Design Decisions

### PDF Layout
- **No field table in narrative mode** — the rendered body text IS the document; a separate table would be redundant and break the legal document feel
- **Minimal footer** — page number only (`"Страница N из N"`) — no URL, no timestamp — keeps it clean
- **QR-only seal section** — verification section has QR + explanatory text; raw URL is intentionally omitted from the visible PDF (QR is sufficient, URL would look unprofessional)
- **Watermark at 6% alpha** — barely visible but legally meaningful; diagonal at 33°

### Security Architecture
- JWT is stateless; access token 30 min, refresh token 24 hr
- Role stored in JWT claim `role` (not authorities list) — single active role per session
- `PersonDetails` wraps `User` + `UserRole` — the specific role binding used to generate the token
- `AbstractAuditingEntity.resolveCurrentEmail()` has try-catch fallback to `"system"` — safe in test contexts

### Schema Management
- **Liquibase only** — `ddl-auto: validate`. Never use `create` or `update` in any environment
- All schema changes require a new changeset XML file in `backend/src/main/resources/liquibase/changelogs/v1/`
- Naming convention: `YYYY-MM-DD-NNN-description.xml`
- Roles (`USER`, `LAWYER`, `ADMIN`) are seeded by `2026-02-24-008-insert-default-roles.xml`

### AI Service
- `ClaudeRuleAIService` is `@Profile("!test")` — never runs in test environment
- Tests use `StubAiService` (`@Profile("test")`) which returns no-op responses
- AI is used for: template matching, field suggestions, document review, clause explanation, required-docs explanation

---

## 9. File Map (Key Files)

```
backend/
├── src/main/java/kz/legeal/ease/backend/
│   ├── controller/
│   │   ├── openApi/
│   │   │   ├── AuthController.java
│   │   │   ├── PublicCategoryController.java
│   │   │   ├── DocumentVerificationController.java  ← NEW
│   │   │   └── PublicShareController.java
│   │   ├── admin/   (AdminAuditController, AdminUserController,
│   │   │            AdminRuleController, AdminMetricsController,
│   │   │            AdminCategoryController, AdminLawyerApplicationController)
│   │   ├── user/    (UserTemplateController, UserDocumentController,
│   │   │            AiController, UserMatchingController,
│   │   │            UserProfileController, UserLawyerApplicationController)
│   │   └── lawyer/  (LawyerTemplateController, LawyerDocumentController,
│   │                 LawyerDocumentReviewController, LawyerMatchingRuleController)
│   ├── service/document/
│   │   ├── DocumentPdfService.java
│   │   ├── DocumentVerificationService.java           ← NEW
│   │   ├── TemplateBodyRenderer.java                  ← NEW
│   │   └── impl/
│   │       ├── DocumentPdfServiceImpl.java            ← REWRITTEN
│   │       ├── DocumentServiceImpl.java               ← MODIFIED (hash)
│   │       └── DocumentVerificationServiceImpl.java   ← NEW
│   ├── domain/
│   │   ├── Document.java       ← MODIFIED (contentHash field)
│   │   └── Template.java       ← MODIFIED (body field)
│   └── dto/document/
│       └── DocumentVerificationResponse.java         ← NEW
├── src/main/resources/
│   ├── fonts/
│   │   ├── FreeSans.ttf
│   │   └── FreeSansBold.ttf
│   └── liquibase/
│       ├── master.xml
│       └── changelogs/v1/
│           ├── 2026-03-28-001-add-template-body.xml  ← NEW
│           └── 2026-03-28-002-add-content-hash-to-documents.xml ← NEW
└── src/test/java/kz/legeal/ease/backend/
    ├── PdfGenerationTest.java
    ├── stub/
    │   └── StubAiService.java                        ← NEW
    └── api/
        ├── BaseIntegrationTest.java                  ← NEW
        ├── AuthApiTest.java                          ← NEW
        ├── PublicApiTest.java                        ← NEW
        ├── AdminApiTest.java                         ← NEW
        ├── LawyerApiTest.java                        ← NEW
        ├── UserDocumentApiTest.java                  ← NEW
        └── SecurityApiTest.java                      ← NEW
```

---

## 10. Running the Project

```bash
# Start all services (PostgreSQL, MinIO, backend, frontend)
docker compose up --build -d

# Backend only (requires docker compose up -d postgres minio)
cd backend && ./gradlew bootRun

# Run all tests (Testcontainers will spin up PostgreSQL automatically)
cd backend && ./gradlew test

# Run only API integration tests
cd backend && ./gradlew test --tests "kz.legeal.ease.backend.api.*"

# Run only PDF generation tests (no Docker required)
cd backend && ./gradlew test --tests "kz.legeal.ease.backend.PdfGenerationTest"

# Frontend dev server
cd frontend && npm run dev
```

**Environment variables** (`.env` at repo root):
- `POSTGRES_*`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`
- `MAIL_*`, `ANTHROPIC_API_KEY`
- `BACKEND_PORT=9191`, `FRONTEND_PORT=3000`
