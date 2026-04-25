# LegalEase

> AI-powered legal document platform for Kazakhstan — users create legally-sound documents from lawyer-authored templates, guided by a multi-stage rule engine, AI analysis, and cryptographic verification.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)

---

## Table of Contents

1. [Platform Overview](#1-platform-overview)
2. [Architecture](#2-architecture)
3. [Domain Model](#3-domain-model)
4. [Document Lifecycle](#4-document-lifecycle)
5. [Rule Engine](#5-rule-engine)
6. [AI Integration](#6-ai-integration)
7. [PDF Generation](#7-pdf-generation)
8. [Storage (MinIO / S3)](#8-storage-minio--s3)
9. [Authentication & Security](#9-authentication--security)
10. [API Reference](#10-api-reference)
11. [Business Rules](#11-business-rules)
12. [Audit Logging](#12-audit-logging)
13. [Frontend Architecture](#13-frontend-architecture)
14. [Tech Stack](#14-tech-stack)
15. [Getting Started](#15-getting-started)
16. [Environment Variables](#16-environment-variables)
17. [Database Migrations](#17-database-migrations)
18. [Testing](#18-testing)
19. [Known Issues](#19-known-issues)
20. [Team](#20-team)

---

## 1. Platform Overview

LegalEase serves three user roles on a single platform:

| Role | What they can do |
|------|-----------------|
| `ROLE_USER` | Browse published templates, create and complete documents, download PDFs, share documents, apply to become a lawyer |
| `ROLE_LAWYER` | Create and publish templates with typed fields and five rule types; browse documents submitted against their templates; manage a clause knowledge base |
| `ROLE_ADMIN` | Manage categories, approve/reject lawyer applications, block/unblock users, toggle rules globally, view the full audit log |

Users who want to become lawyers submit a `LawyerApplication` with their license number. An admin reviews it; on approval the user gains `ROLE_LAWYER`.

---

## 2. Architecture

### Service Topology

```
┌────────────────────────────────────────────────────────────────────┐
│                          Docker Network                            │
│                                                                    │
│  ┌──────────┐  :80    ┌──────────┐         ┌──────────────────┐  │
│  │  Browser │────────▶│  Nginx   │─── / ──▶│  Frontend        │  │
│  └──────────┘         │ (proxy)  │          │  (React 19)      │  │
│                        └──────────┘          └──────────────────┘  │
│                             │                                       │
│                      /api/* │ /open-api/*                          │
│                             ▼                                       │
│                    ┌─────────────────┐  JDBC   ┌──────────────┐   │
│                    │   Backend       │─────────▶│  PostgreSQL  │   │
│                    │ (Spring Boot)   │          │    :5432     │   │
│                    │   :9191         │          └──────────────┘   │
│                    └─────────────────┘                             │
│                             │ AWS SDK v2 (S3 API)                  │
│                             ▼                                       │
│                    ┌─────────────────┐                             │
│                    │     MinIO       │                             │
│                    │   API  :9000    │                             │
│                    │   UI   :9001    │                             │
│                    └─────────────────┘                             │
└────────────────────────────────────────────────────────────────────┘
         │                                 │
      SMTP                           DeepSeek API
      (Gmail)                       (AI, remote)
```

### Nginx Routing Rules

| Path prefix | Upstream |
|-------------|----------|
| `/api/*`, `/open-api/*` | `backend:9191` |
| `/swagger-ui/*`, `/v3/*` | `backend:9191` (OpenAPI) |
| `/` (everything else) | `frontend:80` (React SPA) |

Nginx forwards `Host`, `X-Real-IP`, `X-Forwarded-For`, and `X-Forwarded-Proto` headers. Read/write timeouts: 120 s.

### Backend Package Structure

```
kz.legeal.ease.backend/
├── controller/
│   ├── openApi/         Public: auth, categories, document verify, share
│   ├── user/            ROLE_USER: documents, templates, matching, profile, lawyer-application
│   ├── lawyer/          ROLE_LAWYER: templates, rules, clause library, document review
│   └── admin/           ROLE_ADMIN: users, categories, applications, rules, audit, metrics
├── service/
│   ├── impl/            Business logic implementations
│   ├── rule/
│   │   ├── engine/      RuleEngine — entry point, assembles pipelines
│   │   ├── chain/       Chain-of-Responsibility: RuleChainBuilder, ChainLink interface
│   │   ├── handler/     11 concrete handlers (see §5)
│   │   ├── evaluator/   Low-level operator evaluator (EQUALS, GT, REGEX, …)
│   │   ├── ai/          RuleAiService — all AI prompts
│   │   ├── context/     RuleContext (input) + RuleChainContext (mutable state)
│   │   └── result/      RuleEngineResult (unified output)
│   ├── document/        PDF generation, template rendering, verification, storage
│   └── audit/           Async audit trail
├── domain/              JPA entities
├── dto/                 Response DTOs (MapStruct)
├── request/             Request DTOs (Jakarta Validation)
├── exception/           BaseException hierarchy
├── repository/          Spring Data JPA + Specifications
├── config/              Security, S3, Swagger, Async, Mail, Cache
├── jwt/                 JwtFilter, JwtUtils, PersonDetails
└── storage/             StorageService + S3StorageService
```

---

## 3. Domain Model

### User

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK, sequence |
| `iin` | VARCHAR(12) | Kazakhstan national ID, unique |
| `email` | VARCHAR(100) | unique |
| `phone` | VARCHAR(20) | unique |
| `fio` | VARCHAR(255) | full name |
| `password` | TEXT | BCrypt (strength 12) |
| `active` | BOOLEAN | `false` until email confirmed |
| `deleted` | BOOLEAN | soft-delete flag |
| `date_of_birth` | DATE | optional |
| `gender` | VARCHAR(10) | optional |
| `last_password_modified_date` | TIMESTAMP | password age tracking |
| `created_date`, `updated_date` | TIMESTAMP | auditing |

### Role

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `code` | VARCHAR(50) | unique — `USER`, `LAWYER`, `ADMIN` |
| `active` | BOOLEAN | |

### UserRole

Join table linking users to their active roles.

| Column | Type |
|--------|------|
| `id` | BIGINT PK |
| `user_id` | FK → users |
| `role_id` | FK → role |
| `active` | BOOLEAN |

### Template

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `title` | VARCHAR(255) | required |
| `description` | TEXT | optional |
| `body` | TEXT | optional — narrative body with `{{field_key}}` placeholders |
| `category_id` | BIGINT | FK → categories |
| `status` | VARCHAR(20) | `DRAFT` \| `PUBLISHED` |
| `lawyer_id` | BIGINT | FK → users (owner) |
| `active` | BOOLEAN | |

**Body placeholders** use double-curly syntax: `{{client_name}}`, `{{contract_date}}`. Missing keys are rendered as `[key]` (never blank, never error).

### TemplateField

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `template_id` | BIGINT | FK → templates |
| `field_key` | VARCHAR(100) | unique per template (snake_case) |
| `label` | VARCHAR(255) | display label |
| `field_type` | VARCHAR(20) | `TEXT` \| `DATE` \| `NUMBER` \| `SELECT` |
| `required` | BOOLEAN | |
| `order_num` | INTEGER | display order in form |
| `active` | BOOLEAN | |

### Document

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK → users (owner) |
| `template_id` | BIGINT | FK → templates |
| `title` | VARCHAR(255) | user-provided |
| `status` | VARCHAR(20) | `DRAFT` \| `VALIDATED` \| `COMPLETED` \| `ARCHIVED` |
| `s3_object_key` | VARCHAR(512) | null for DRAFT; path in MinIO |
| `current_version` | INTEGER | 0 = never completed; increments each `complete()` |
| `content_hash` | VARCHAR(64) | SHA-256 hex of latest PDF; null until first `complete()` |
| `deleted` | BOOLEAN | soft-delete flag |

### DocumentFieldValue

Key-value pairs storing user inputs.

| Column | Type |
|--------|------|
| `id` | BIGINT PK |
| `document_id` | FK → documents |
| `field_key` | VARCHAR(100) |
| `field_value` | TEXT |

### DocumentVersion

Immutable snapshot created on every `complete()`.

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `document_id` | BIGINT | FK → documents |
| `version` | INTEGER | matches `Document.current_version` at creation time |
| `s3_object_key` | VARCHAR(512) | exact S3 path for this version |
| `created_date` | TIMESTAMP | |

### DocumentShare

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `document_id` | BIGINT | FK → documents |
| `share_token` | VARCHAR(255) | UUID-based cryptographically secure token; unique |
| `expires_at` | TIMESTAMP | configurable TTL (default 48 h) |
| `active` | BOOLEAN | set `false` when document is archived |

### Category

| Column | Type |
|--------|------|
| `id` | BIGINT PK |
| `name` | VARCHAR(100) unique |
| `description` | TEXT |
| `active` | BOOLEAN |

### Rule Entities

All rule entities share: `id`, `template_id` (FK), `active` (BOOLEAN), `created_date`, `created_by`.

#### MatchingRule

| Column | Notes |
|--------|-------|
| `keywords` | TEXT — CSV keyword list (e.g., `"аренда, жильё, договор"`) |
| `base_score` | INTEGER — baseline relevance score (default 50) |
| `category_id` | FK → categories (optional scope filter) |

#### ValidationRule

| Column | Notes |
|--------|-------|
| `field_key` | Field to validate |
| `field_label` | Display label |
| `operator` | `RuleConditionOperator` value |
| `expected_value` | Right-hand operand |
| `error_message` | Shown to user on failure |

#### RiskRule

| Column | Notes |
|--------|-------|
| `rule_code` | Unique code (e.g., `RISK_HIGH_AMOUNT`) |
| `field_key` | Field to evaluate (nullable — unconditional risks) |
| `operator` | `RuleConditionOperator` value |
| `expected_value` | Threshold value |
| `risk_message` | TEXT — risk description |
| `risk_level` | `HIGH` \| `MEDIUM` \| `LOW` |

#### ConditionRule

| Column | Notes |
|--------|-------|
| `condition_field_key` | Field whose value triggers the rule |
| `operator` | Comparison operator |
| `condition_value` | Trigger value |
| `target_field_key` | Field that becomes required if condition is true |

**Purpose:** Dynamically marks fields as required based on current user inputs (e.g., "if `payment_type = INSTALLMENT` then `installment_count` is required").

#### RequiredDocRule

| Column | Notes |
|--------|-------|
| `required_doc_title` | Name of document (e.g., "Copy of Passport") |
| `reason` | Base explanation (enriched by AI) |
| `condition_field_key` | Nullable — if null, document is always required |
| `condition_operator` | Comparison operator |
| `condition_value` | Trigger value |
| `mandatory` | `true` = hard requirement; `false` = advisory |

**Purpose:** Define what supporting documents a user must provide, either always or based on field values.

#### RuleConditionOperator (Enum)

```
IS_EMPTY        value is blank
IS_NOT_EMPTY    value is not blank
EQUALS          value == expected
NOT_EQUALS      value != expected
CONTAINS        value contains expected (substring)
GREATER_THAN    numeric value > expected
LESS_THAN       numeric value < expected
GREATER_OR_EQUAL  >=
LESS_OR_EQUAL     <=
```

### LegalClause (Lawyer Knowledge Base)

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `lawyer_id` | BIGINT | FK → users (owner) |
| `title` | VARCHAR(255) | e.g., "Standard Force Majeure Clause" |
| `content` | TEXT | full clause text |
| `tags` | TEXT | CSV-separated tags (e.g., `"force_majeure,liability"`) |
| `category_id` | BIGINT | FK → categories (optional) |
| `active` | BOOLEAN | |

### LawyerApplication

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK → users |
| `license_number` | VARCHAR(100) | unique |
| `status` | VARCHAR(20) | `PENDING` \| `APPROVED` \| `REJECTED` |
| `submitted_at` | TIMESTAMP | |
| `reviewed_by` | BIGINT | FK → users (admin reviewer) |
| `reviewed_at` | TIMESTAMP | |
| `rejection_reason` | TEXT | nullable |
| `version` | INTEGER | optimistic locking |

### AuditLog

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | nullable (system actions) |
| `action` | VARCHAR(60) | `AuditAction` enum value |
| `entity_type` | VARCHAR(60) | e.g., `"Document"`, `"Template"` |
| `entity_id` | BIGINT | nullable |
| `metadata` | TEXT | optional JSON (version, token, reason) |
| `created_at` | TIMESTAMP | immutable |

**AuditAction values:**

```
Document lifecycle:
  DOCUMENT_CREATED, DOCUMENT_UPDATED, DOCUMENT_VALIDATED,
  DOCUMENT_COMPLETED, DOCUMENT_ARCHIVED, DOCUMENT_RESTORED, DOCUMENT_DELETED

Document access:
  DOCUMENT_DOWNLOADED, DOCUMENT_VERSION_DOWNLOADED,
  DOCUMENT_SHARED, DOCUMENT_SHARE_REVOKED, DOCUMENT_AI_ANALYZED

Lawyer actions:
  DOCUMENT_REVIEWED

User management:
  USER_BLOCKED, USER_UNBLOCKED, USER_ROLE_REVOKED

Lawyer applications:
  LAWYER_APPLICATION_SUBMITTED, LAWYER_APPLICATION_APPROVED, LAWYER_APPLICATION_REJECTED

Rule management:
  RULE_TOGGLED, RULE_CREATED, RULE_UPDATED, RULE_DELETED

Clause knowledge base:
  CLAUSE_CREATED, CLAUSE_UPDATED, CLAUSE_DELETED
```

---

## 4. Document Lifecycle

```
                         ┌──────────────────────────────────────────────┐
                         │                  DRAFT                       │
                         │  • Field values editable                     │
                         │  • No PDF exists yet                         │
                         │  • Can call /suggestions for AI hints        │
                         └──────────────────────────────────────────────┘
                                            │
                              POST /{id}/validate
                        (runs: Validation → Risk → RequiredDocs → AI)
                                            │
                                    valid=true?
                                   ╱          ╲
                                 YES           NO
                                  │             │
                                  ▼             ▼
                          ┌──────────┐   stays DRAFT,
                          │VALIDATED │   returns validationErrors
                          └──────────┘
                                  │
                     POST /{id}/complete
           (re-runs full pipeline + AI final review + PDF)
                                  │
                                  ▼
                         ┌──────────────────────────────────────────────┐
                         │                COMPLETED                     │
                         │  • PDF generated (PDFBox)                    │
                         │  • Uploaded to MinIO (documents/{id}/v{n})   │
                         │  • SHA-256 content hash stored               │
                         │  • current_version incremented               │
                         │  • Immutable DocumentVersion created         │
                         │  • Can download, share, get presigned URL    │
                         └──────────────────────────────────────────────┘
                                  │                     │
                  POST /{id}/archive             POST /{id}/regenerate-pdf
                       │                    (overwrite S3, same version)
                       ▼
              ┌─────────────────────────────────────────────────────────┐
              │                    ARCHIVED                             │
              │  • Hidden from default document list                    │
              │  • All active share links revoked                       │
              │  • PDF still accessible in S3 (versions preserved)      │
              └─────────────────────────────────────────────────────────┘
                       │
               POST /{id}/restore
                       │
                       ▼
                  back to DRAFT
             (version history preserved)
```

### Status Transition Rules

| From | To | Trigger | Guard |
|------|----|---------|-------|
| `DRAFT` | `VALIDATED` | `POST /{id}/validate` | All validation rules pass |
| `DRAFT` | `DRAFT` | `POST /{id}/validate` | Any validation rule fails |
| `VALIDATED` | `COMPLETED` | `POST /{id}/complete` | Full pipeline passes |
| `COMPLETED` | `ARCHIVED` | `POST /{id}/archive` | — |
| `ARCHIVED` | `DRAFT` | `POST /{id}/restore` | — |
| `DRAFT` | `DRAFT` | `PUT /{id}` | Field update (resets to DRAFT if VALIDATED) |

---

## 5. Rule Engine

### Architecture

The rule engine uses the **Chain of Responsibility** pattern. Each pipeline is an ordered list of `ChainLink` handlers. `RuleChainBuilder` assembles the correct chain by name. Each handler reads from and writes to a shared `RuleChainContext`. Any handler can call `context.abort(reason)` to halt the chain early.

### Shared Context

```java
// Input (immutable, set before chain runs)
RuleContext {
    Long     documentId;
    Long     templateId;
    String   userInputText;       // free-text for matching
    Map<String,String> fieldValues;
    String   documentBodyText;    // rendered body for AI review
}

// Mutable state (populated by handlers)
RuleChainContext {
    RuleContext                input;

    List<MatchedTemplate>      matchedTemplates;
    List<ValidationError>      validationErrors;
    List<RiskItem>             risks;
    List<String>               requiredDynamicFields;
    List<RequiredDocument>     requiredDocuments;
    List<FieldSuggestion>      fieldSuggestions;

    String  aiSummary;
    String  aiRecommendation;
    String  aiIntentLabel;
    double  aiIntentConfidence;

    boolean valid;
    boolean aborted;
    String  abortReason;
}
```

### Final Output

```java
RuleEngineResult {
    List<MatchedTemplate>  matchedTemplates;   // ranked templates (matching pipeline)
    List<ValidationError>  validationErrors;   // field failures (complete pipeline)
    List<RiskItem>         risks;              // risk flags with optional AI explanation
    List<String>           requiredDynamicFields;
    List<RequiredDocument> requiredDocuments;
    List<FieldSuggestion>  fieldSuggestions;

    boolean  valid;
    boolean  aborted;
    String   abortReason;

    String   aiSummary;
    String   aiRecommendation;
    String   aiIntentLabel;
    double   aiIntentConfidence;
}
```

### Pipeline 1 — Matching Chain

Triggered by `POST /api/user/matching` — user provides free-text; returns ranked templates.

```
AIUnderstandingHandler
  └─ Calls AI to detect legal intent (e.g., "Lease Agreement") and extract entities
  └─ Sets: context.aiIntentLabel, aiIntentConfidence

MatchingHandler
  └─ Loads all active MatchingRules
  └─ For each template: score = baseScore + (keyword overlap count × 10)
  └─ Filters score > 0; sorts descending
  └─ Sets: context.matchedTemplates (preliminary ranking)

AIIntentFallbackHandler
  └─ Only runs if intent confidence < threshold
  └─ AI re-tries intent detection with richer context
  └─ Adjusts scores for templates in detected category

AiRankingHandler
  └─ Takes top-N candidates from context.matchedTemplates
  └─ Sends to AI for semantic re-ranking
  └─ Updates ordering + adds aiNote per candidate
```

### Pipeline 2 — Suggestion Chain

Triggered by `GET /api/user/documents/{id}/suggestions` — returns field hints and required docs.

```
ConditionalHandler
  └─ Loads all active ConditionRules for the template
  └─ Evaluates each: if condition holds → adds targetFieldKey to requiredDynamicFields
  └─ Sets: context.requiredDynamicFields

RequiredDocsHandler
  └─ Loads all active RequiredDocRules
  └─ For unconditional rules (conditionFieldKey = null): always adds to list
  └─ For conditional rules: evaluates condition against current fieldValues
  └─ Sets: context.requiredDocuments (with reason from rule)

AIDocsExplainerHandler
  └─ Takes context.requiredDocuments
  └─ For each doc: calls AI to enrich reason with Kazakhstan-law context
  └─ Updates reason fields with AI-generated text

AIFieldSuggestionHandler
  └─ Calls AI with: template context + current fieldValues + empty field keys
  └─ AI suggests values with rationale
  └─ Sets: context.fieldSuggestions [{fieldKey, label, suggestedValue, reason}]
```

### Pipeline 3 — Completion Chain

Triggered by `POST /api/user/documents/{id}/complete` — full validation, AI review, then PDF.

```
ValidationHandler
  └─ Loads all active ValidationRules for the template
  └─ Evaluates each rule's operator against the field's current value
  └─ Collects all failures as ValidationError {fieldKey, label, message}
  └─ If any failure: sets context.valid = false, ABORTS chain
  └─ Sets: context.validationErrors

RiskHandler  (only runs if valid = true)
  └─ Loads all active RiskRules
  └─ Evaluates each condition; collects matches as RiskItem {code, message, level}
  └─ Does NOT abort (risks are warnings, not blockers)
  └─ Sets: context.risks

RequiredDocsHandler
  └─ Same logic as in suggestion chain

AIDocsExplainerHandler
  └─ Same logic as in suggestion chain

AIRiskExplainerHandler
  └─ Takes context.risks
  └─ For each risk: calls AI to provide plain-language legal explanation
  └─ Updates context.risks[i].aiExplanation

AIFinalReviewHandler
  └─ Calls AI with the rendered document body text
  └─ AI produces: summary, obligations, warnings, recommendations
  └─ Sets: context.aiSummary, context.aiRecommendation
```

### Condition Evaluator

```
IS_EMPTY         → value.isBlank()
IS_NOT_EMPTY     → !value.isBlank()
EQUALS           → value.equalsIgnoreCase(expected)
NOT_EQUALS       → !value.equalsIgnoreCase(expected)
CONTAINS         → value.toLowerCase().contains(expected.toLowerCase())
GREATER_THAN     → parseDouble(value) > parseDouble(expected)
LESS_THAN        → parseDouble(value) < parseDouble(expected)
GREATER_OR_EQUAL → >=
LESS_OR_EQUAL    → <=
```

---

## 6. AI Integration

All AI calls go through `RuleAiService`. The real implementation (`ClaudeRuleAiServiceImpl`) is annotated `@Profile("!test")` — tests use a `StubAiService` (`@Profile("test")`) that returns no-op responses.

The backend calls the **DeepSeek API** (`DEEPSEEK_API_KEY`) via plain REST (`RestTemplate`). Responses are stripped of markdown formatting before parsing.

### Methods

| Method | Triggered by | Input | Output |
|--------|-------------|-------|--------|
| `detectIntent(text)` | Matching pipeline | User's free-text query | `{intentLabel, confidence}` |
| `rankTemplates(candidates, intentLabel)` | Matching pipeline | Top-N template summaries | Re-ordered list + per-template AI note |
| `suggestFields(template, fieldValues)` | Suggestion pipeline | Template metadata + current values | `List<FieldSuggestion>` |
| `explainRequiredDocs(docs, templateCtx)` | Suggestion + completion | Required doc list | Enriched reasons (Kazakhstan law context) |
| `explainRisks(risks, documentText)` | Completion pipeline | Risk items + document text | AI explanations per risk |
| `finalReview(documentText, templateTitle)` | Completion pipeline | Rendered document body | `{summary, recommendation}` |
| `explainClause(clauseText)` | `POST /api/ai/explain-clause` | Legal clause text | `{explanation, risks[], recommendations[]}` |
| `explainDocument(documentText, title)` | `GET /api/user/documents/{id}/explain` | Full document text | `{summary, obligations[], warnings[], nextSteps[]}` |

---

## 7. PDF Generation

`DocumentPdfServiceImpl` supports two rendering modes selected at runtime:

**Narrative mode** — template has a `body` field with `{{placeholders}}`:
1. `TemplateBodyRenderer.render(body, fieldValues)` substitutes all placeholders; missing keys become `[key]`
2. Rendered text is laid out as flowing justified paragraphs

**Legacy mode** — template has no body:
- Renders a field label → value table (backwards compatibility with old templates)

### PDF Structure

```
┌────────────────────────────────────────────────────────┐
│  HEADER  (page top, 50pt margin)                        │
│  "LegalEase"  bold 18pt           DOC-000042           │
│  "Юридический документ"           28.04.2026           │
├────────────────────────────────────────────────────────┤
│  TITLE BLOCK                                            │
│  ── document title, bold 16pt, centered ──             │
│  ─────────────────────────────────────────────────     │
│                                                         │
│  BODY  (narrative or field table)                       │
│  11pt FreeSans, justified, line-wrapped                 │
│  Cyrillic text rendered correctly (CID TrueType)       │
│                                                         │
│  ┌ VERIFICATION SEAL ──────────────────────────────┐   │
│  │  ┌──────────┐   Документ создан через            │   │
│  │  │  QR CODE │   платформу LegalEase.             │   │
│  │  │  80×80pt │   Отсканируйте QR-код для          │   │
│  │  └──────────┘   проверки подлинности.            │   │
│  └─────────────────────────────────────────────────┘   │
├────────────────────────────────────────────────────────┤
│  FOOTER  "Страница N из N"  centered 9pt               │
│  WATERMARK  diagonal "LegalEase"  6% alpha  33°        │
└────────────────────────────────────────────────────────┘
```

### Fonts

Both fonts are embedded as `PDType0Font` (CID TrueType, Identity-H encoding) to guarantee full Cyrillic rendering. Located at `backend/src/main/resources/fonts/`.

| File | Usage |
|------|-------|
| `FreeSans.ttf` | Body text, footer |
| `FreeSansBold.ttf` | Headings, labels |

### QR Code

Generated by ZXing `QRCodeWriter`. Content: `{app.base-url}/documents/verify/{documentId}`. Error correction level M, margin 1, rendered at 80 × 80 pt via PDFBox's `LosslessFactory`.

### Content Hash

After PDF bytes are generated, `DocumentServiceImpl.complete()` computes:

```java
String hash = HexFormat.of().formatHex(
    MessageDigest.getInstance("SHA-256").digest(pdfBytes)
);
document.setContentHash(hash);
```

The hash is stored in `Document.content_hash`. Its presence indicates a properly completed document at the public verification endpoint.

---

## 8. Storage (MinIO / S3)

### Object Key Pattern

```
documents/{documentId}/v{version}.pdf
```

Examples:
```
documents/42/v1.pdf   ← first completion
documents/42/v2.pdf   ← second completion (after update + re-complete)
```

### StorageService Interface

```java
String uploadFile(String key, byte[] data, String contentType);
byte[] downloadFile(String key);
void   deleteFile(String key);
String generatePresignedUrl(String key, Duration ttl);
```

`S3Config` auto-creates the bucket (`STORAGE_S3_BUCKET`) on application startup if it does not exist.

### Constraints

- Max file size: **20 MB**
- Accepted content type: `application/pdf` only
- Presigned URL default TTL: **60 minutes**

### Internal vs Public Endpoints

The backend uses two S3 endpoints:
- **`STORAGE_S3_ENDPOINT`** (`http://minio:9000`) — internal Docker network, used for all upload/download operations
- **`STORAGE_S3_PUBLIC_ENDPOINT`** (`http://localhost:9000`) — used when generating presigned URLs (must be reachable from the browser)

---

## 9. Authentication & Security

### Token Strategy

| Token | Signing key | Default TTL | Purpose |
|-------|------------|-------------|---------|
| Access | `JWT_ACCESS_SECRET` | 30 min | Authorise API calls |
| Refresh | `JWT_REFRESH_SECRET` | 24 h | Obtain new access token |

Both tokens are signed with HMAC-SHA256. The access token carries claims `userId` and `role`. The refresh token carries `userId` and `type=refresh`.

Refresh tokens are persisted in the `refresh_token` table; logout invalidates the row.

### Request Flow

```
Browser  →  Authorization: Bearer <token>
               ↓
          JwtFilter.doFilterInternal()
            1. Extract Bearer token
            2. JwtUtils.extractUserId(token)
            3. Load PersonDetails from DB
            4. JwtUtils.isTokenValid(token, personDetails)
            5. Set SecurityContextHolder
               ↓
          Controller → Service → ...
```

### Security Configuration

```
CSRF:       disabled (stateless)
Sessions:   STATELESS
CORS:       all origins (⚠ must be restricted for production)

Public paths:
  /open-api/**
  /api/public/**
  /swagger-ui/**
  /v3/api-docs/**

Protected paths:
  /api/user/**    → ROLE_USER
  /api/lawyer/**  → ROLE_LAWYER
  /api/admin/**   → ROLE_ADMIN
  /api/ai/**      → any authenticated role

401 handler: returns JSON error (no redirect to login page)
```

### Email Verification

On registration, a 6-digit code is sent via SMTP. The user must call `POST /open-api/auth/confirm` with the code to activate their account. Password reset uses the same mechanism.

---

## 10. API Reference

### Public Endpoints (no JWT required)

#### Auth — `POST /open-api/auth/*`

| Method | Path | Request body | Response |
|--------|------|-------------|---------|
| POST | `/login` | `{email, password}` | `{accessToken, refreshToken, expiresIn}` |
| POST | `/register` | `{firstName, middleName?, lastName, iin, gender, email, phone, password}` | 200 OK |
| POST | `/confirm` | `{email, code}` | 200 OK |
| POST | `/refresh` | `{refreshToken}` | `{accessToken, refreshToken, expiresIn}` |
| POST | `/reset-password` | `{email}` | 200 OK (sends code) |
| POST | `/change-password` | `{email, code, newPassword}` | 200 OK |
| POST | `/logout` | `{refreshToken}` | 200 OK |

#### Public Categories

| Method | Path | Response |
|--------|------|---------|
| GET | `/open-api/categories` | `List<CategoryDto>` |

#### Document Verification (QR target)

| Method | Path | Response |
|--------|------|---------|
| GET | `/open-api/documents/verify/{id}` | `{status: VALID|INVALID, documentId?, createdAt?, createdBy?, hashValid}` |

#### Public Share

| Method | Path | Response |
|--------|------|---------|
| GET | `/api/public/share/{token}` | `application/pdf` bytes |

---

### User Endpoints (`ROLE_USER`)

#### Document Management — `/api/user/documents`

| Method | Path | Notes |
|--------|------|-------|
| POST | `/` | Create document; body: `{templateId, title, fieldValues?}` |
| GET | `/` | List my active documents (paginated, excludes ARCHIVED) |
| GET | `/all` | List all my documents including ARCHIVED |
| GET | `/{id}` | Get document detail |
| PUT | `/{id}` | Update DRAFT document; body: `{title?, fieldValues?}` |
| DELETE | `/{id}` | Soft-delete DRAFT document |
| GET | `/{id}/suggestions` | Run suggestion pipeline → `RuleEngineResult` |
| POST | `/{id}/validate` | Run validation; if passes: status → `VALIDATED` |
| POST | `/{id}/complete` | Generate PDF, upload to S3, status → `COMPLETED` |
| GET | `/{id}/explain` | AI plain-language explanation → `DocumentExplainResponse` |
| POST | `/{id}/archive` | Status → `ARCHIVED`; revokes all shares |
| POST | `/{id}/restore` | Status → `DRAFT`; version history preserved |
| POST | `/{id}/regenerate-pdf` | Re-generate PDF in-place (same version) |
| GET | `/{id}/download` | Stream latest PDF bytes |
| GET | `/{id}/url` | Presigned S3 URL (default TTL: 60 min) |
| POST | `/{id}/share` | Create share link → `{shareToken, shareUrl, expiresAt}` |
| GET | `/{id}/versions` | List all `DocumentVersion` records |
| GET | `/{id}/versions/{v}/download` | Download specific version PDF |

#### Template Browse — `/api/user/templates`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | Published templates; query: `?categoryId=&page=&size=` |
| GET | `/{id}` | Single published template detail |

#### Template Matching — `/api/user/matching`

| Method | Path | Notes |
|--------|------|-------|
| POST | `/` | Body: `{description, categoryId?}` → `RuleEngineResult` with ranked `matchedTemplates` |

#### Profile — `/api/user/profile`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | My profile |
| PUT | `/` | Update profile |

#### Lawyer Application — `/api/user/lawyer-applications`

| Method | Path | Notes |
|--------|------|-------|
| POST | `/` | Submit application; body: `{licenseNumber}` |
| GET | `/my` | Get my application status |

---

### Lawyer Endpoints (`ROLE_LAWYER`)

#### Template Management — `/api/lawyer/templates`

| Method | Path | Notes |
|--------|------|-------|
| POST | `/` | Create template; body: `{title, description, body?, categoryId, fields[]}` |
| GET | `/` | My templates (paginated) |
| GET | `/{id}` | Template detail |
| PUT | `/{id}` | Update DRAFT template |
| POST | `/{id}/publish` | DRAFT → PUBLISHED |
| DELETE | `/{id}` | Delete DRAFT template |

#### Template field object:
```json
{ "fieldKey": "client_name", "label": "Client Name", "fieldType": "TEXT", "required": true }
```

#### Document Review — `/api/lawyer/documents`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | Documents from my published templates (paginated) |
| GET | `/by-template/{templateId}` | Filter by template |
| GET | `/{id}` | Document detail (read-only) |

#### Rule Management

All rule endpoints follow the same CRUD pattern: `POST /`, `GET /template/{templateId}`, `PUT /{id}`, `DELETE /{id}`.

| Base path | Rule type | Key fields in request |
|-----------|-----------|----------------------|
| `/api/lawyer/validation-rules` | ValidationRule | `fieldKey, operator, expectedValue, errorMessage` |
| `/api/lawyer/risk-rules` | RiskRule | `fieldKey, operator, expectedValue, riskMessage, riskLevel` |
| `/api/lawyer/condition-rules` | ConditionRule | `conditionFieldKey, operator, conditionValue, targetFieldKey` |
| `/api/lawyer/required-doc-rules` | RequiredDocRule | `requiredDocTitle, reason, conditionFieldKey?, conditionOperator?, conditionValue?, mandatory` |
| `/api/lawyer/matching-rules` | MatchingRule | `keywords (CSV), baseScore` |

#### Clause Library — `/api/lawyer/clauses`

| Method | Path | Notes |
|--------|------|-------|
| POST | `/` | Create clause; body: `{title, content, tags, categoryId?}` |
| GET | `/` | My clauses |
| GET | `/{id}` | Clause detail |
| PUT | `/{id}` | Update clause |
| DELETE | `/{id}` | Delete clause |

---

### Admin Endpoints (`ROLE_ADMIN`)

#### Category Management — `/api/admin/categories`

| Method | Path | Notes |
|--------|------|-------|
| POST | `/` | Create; body: `{name, description}` |
| GET | `/` | List (paginated) |
| GET | `/{id}` | Detail |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Deactivate |

#### User Management — `/api/admin/users`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | All users (paginated) |
| GET | `/{id}` | User detail |
| POST | `/{id}/block` | Set `user.active = false` |
| POST | `/{id}/unblock` | Set `user.active = true` |
| POST | `/{id}/revoke-lawyer` | Remove LAWYER role |

#### Lawyer Applications — `/api/admin/lawyer-applications`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | All applications; search: `?status=&userId=&page=&size=` |
| GET | `/{id}` | Application detail |
| POST | `/{id}/approve` | PENDING → APPROVED; user gains `ROLE_LAWYER` |
| POST | `/{id}/reject` | PENDING → REJECTED; body: `{reason}` |
| DELETE | `/{id}` | Delete application record |

#### Rule Engine Management — `/api/admin/rules`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | All rules across all templates |
| POST | `/{ruleType}/{id}/toggle` | Enable/disable a rule globally |

#### Audit Log — `/api/admin/audit`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/search` | Query: `?userId=&action=&entityType=&entityId=&page=&size=` |

#### Metrics — `/api/admin/metrics`

| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | Platform-wide statistics (user count, document count, etc.) |

---

### AI Endpoints (any authenticated role)

| Method | Path | Request body | Response |
|--------|------|-------------|---------|
| POST | `/api/ai/explain-clause` | `{text}` | `{explanation, risks[], recommendations[]}` |

---

## 11. Business Rules

### 1. Template Immutability After Publish

Once a template is `PUBLISHED`, it cannot be edited. Lawyers who need to change a published template must create a new one. This protects documents that were already created from that template.

### 2. Document Field Validation

The `ValidationHandler` aborts the entire completion pipeline on the first batch of failed rules. `RuleEngineResult.valid = false` is returned and the document stays in its current status. All failures are collected and returned together (not one at a time).

### 3. Risk Flags Are Non-Blocking

`RiskHandler` never aborts the chain. Even HIGH-severity risks only add warnings to `context.risks`. The user sees them in the UI and can proceed with the completion. AI enrichment is added on top.

### 4. Required Documents vs. Conditional Documents

- A `RequiredDocRule` with `conditionFieldKey = null` is **always required** regardless of field values.
- A `RequiredDocRule` with a condition is only surfaced when the condition evaluates to true.
- `mandatory = true` means the user must confirm they have the document. `mandatory = false` is advisory.

### 5. Conditional Field Requirements

`ConditionRule` dynamically expands the set of required fields at runtime. For example: if `payment_type = INSTALLMENT` then `installment_count` becomes required. The `ConditionalHandler` appends such keys to `context.requiredDynamicFields`, which the frontend renders as additional mandatory inputs.

### 6. Document Archiving and Shares

When a document is archived:
- All `DocumentShare` rows for that document have `active` set to `false`.
- Requests to `GET /api/public/share/{token}` where the share is inactive return 404.
- The PDF is not deleted from S3; it remains accessible to the owner.

### 7. Document Versioning

Every successful call to `POST /{id}/complete` increments `current_version` and stores a new immutable `DocumentVersion` row pointing to the new S3 object. Old versions are never deleted; they can be downloaded individually via `GET /{id}/versions/{v}/download`.

### 8. Content Verification (QR)

When a document is completed, a QR code is embedded in the PDF. The QR encodes the URL `{base-url}/documents/verify/{documentId}`. The public endpoint at that URL checks whether `content_hash` is non-null:
- `content_hash` present → `{status: "VALID", hashValid: true, ...}`
- `content_hash` absent / document deleted → `{status: "INVALID", hashValid: false}`

This allows third parties to verify a document was genuinely created and finalised through the platform by scanning the QR.

### 9. Lawyer Application Workflow

1. User submits application with license number (`PENDING`).
2. Admin reviews via admin UI.
3. **Approve** → user's `UserRole` record for `LAWYER` is created (or reactivated). Admin confirmed as reviewer.
4. **Reject** → status set to `REJECTED` with optional reason. User can re-apply.
5. Admin can revoke the lawyer role at any time via `POST /api/admin/users/{id}/revoke-lawyer`.

### 10. Rate Limiting

An in-process `RateLimitService` (backed by `ConcurrentHashMap`) limits requests per IP per endpoint. **Note:** not distributed — resets on restart. Must be replaced with Redis/Bucket4j before multi-instance deployment.

---

## 12. Audit Logging

All significant events are logged **asynchronously** via Spring's `@Async` executor to avoid blocking the main request thread.

The `AuditService.log(userId, action, entityType, entityId, metadata)` method creates an `AuditLog` row. The `metadata` column holds optional JSON for extra context (e.g., `{"version": 2}` for a download, `{"reason": "..."} ` for a rejection).

Audit logs are:
- **Immutable** — never updated or deleted
- **Searchable** by admin via `GET /api/admin/audit/search` with filters for `userId`, `action`, `entityType`, `entityId`
- Paginated with `page` / `size` / `sort` query parameters

---

## 13. Frontend Architecture

### State Management (Redux Toolkit)

The Redux store is split into feature slices:

| Slice | State held |
|-------|-----------|
| `authSlice` | `accessToken`, `refreshToken`, `isAuthenticated`, `role` — persisted to `localStorage`; role decoded from JWT payload |
| RTK Query APIs | Server state via cache (documents, templates, categories, rules, admin data) |

### RTK Query API Modules

| Module | Endpoints served |
|--------|----------------|
| `authApi` | login, register, confirm, refresh, reset-password, change-password, logout |
| `documentApi` | all document CRUD + lifecycle actions + versions + share + download |
| `templateApi` | public template browse + lawyer template CRUD + publish |
| `categoryApi` | category list |
| `matchingApi` | POST matching |
| `lawyerRuleApi` | CRUD for all 5 rule types |
| `lawyerClauseApi` | clause library CRUD |
| `adminApi` | users, categories, lawyer applications, audit logs, rule toggles, metrics |
| `aiApi` | explain-clause |

### Axios Instance

- Base URL: `VITE_API_URL` environment variable
- Request interceptor: attaches `Authorization: Bearer <accessToken>` from Redux store
- Response interceptor: on 401, attempts one token refresh, then redirects to `/login` on failure

### Page Map

| Page | Route | Role |
|------|-------|------|
| Login | `/login` | Public |
| Register | `/register` | Public |
| Email Confirm | `/confirm` | Public |
| Forgot Password | `/forgot-password` | Public |
| Reset Password | `/reset-password` | Public |
| Document Verify | `/documents/verify/:id` | Public |
| Dashboard | `/` | USER |
| Documents | `/documents` | USER |
| Document Detail | `/documents/:id` | USER |
| Template Matching | `/matching` | USER |
| User Profile | `/profile` | USER |
| Lawyer Application | `/lawyer-application` | USER |
| Lawyer Templates | `/lawyer/templates` | LAWYER |
| Lawyer Documents | `/lawyer/documents` | LAWYER |
| Rule Manager | `/lawyer/templates/:id/rules` | LAWYER |
| Clause Library | `/lawyer/clauses` | LAWYER |
| Admin Dashboard | `/admin` | ADMIN |
| Admin Users | `/admin/users` | ADMIN |
| Admin Categories | `/admin/categories` | ADMIN |
| Admin Applications | `/admin/applications` | ADMIN |
| Admin Audit Log | `/admin/audit` | ADMIN |

### Routing & Auth Guard

React Router v7 with a protected route wrapper that reads `isAuthenticated` and `role` from the Redux store. Unauthenticated requests are redirected to `/login`. Role-specific routes redirect users to their default dashboard if the wrong role is detected.

---

## 14. Tech Stack

### Backend

| Concern | Library / Version |
|---------|------------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security + JJWT 0.11.5 |
| Persistence | Spring Data JPA + Hibernate |
| Schema migrations | Liquibase |
| DTO mapping | MapStruct 1.5.5 |
| Boilerplate reduction | Lombok |
| API documentation | SpringDoc OpenAPI 2.8.5 |
| PDF generation | Apache PDFBox 3.0.3 |
| QR codes | ZXing 3.5.3 |
| Object storage | AWS SDK v2 (MinIO / S3-compatible) |
| Caching | Caffeine (in-process) |
| AI | DeepSeek API (REST via RestTemplate) |
| Email | Spring Mail + JavaMail (SMTP) |
| Validation | Jakarta Validation (Hibernate Validator) |
| Testing | JUnit 5 + Testcontainers + MockMvc + GreenMail |
| Build | Gradle |

### Frontend

| Concern | Library / Version |
|---------|------------------|
| Framework | React 19 |
| Language | TypeScript 5.9 |
| Build | Vite 7 |
| State | Redux Toolkit + RTK Query |
| Routing | React Router v7 |
| UI | Ant Design 6 + Tailwind CSS 3 |
| Forms | Ant Design Form + Zod validation |
| HTTP | Axios 1.x with JWT interceptors |
| Icons | Lucide React |

### Infrastructure

| Concern | Technology |
|---------|-----------|
| Database | PostgreSQL 16 |
| Object storage | MinIO (latest) |
| Reverse proxy | Nginx (Alpine) |
| Containers | Docker + Docker Compose v2 |

---

## 15. Getting Started

### Prerequisites

| Tool | Minimum |
|------|---------|
| Docker | 24+ |
| Docker Compose | 2.20+ |
| Java | 21+ (local dev only) |
| Node.js | 20+ (local dev only) |

### Local Development

Run PostgreSQL and MinIO in Docker; start backend and frontend locally.

```bash
# 1. Clone
git clone https://github.com/your-org/legal-ease.git
cd legal-ease

# 2. Configure secrets
cp .env.example .env
# Open .env and fill in all required values (see §16)

# 3. Start only infrastructure
docker compose up postgres minio -d

# 4. Start backend (Spring DevTools hot reload)
cd backend
./gradlew bootRun

# 5. Start frontend (Vite HMR)
cd ../frontend
npm install
npm run dev
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:9191 |
| Swagger UI | http://localhost:9191/swagger-ui/index.html |
| MinIO Console | http://localhost:9001 |

### Full Docker Stack

```bash
# Build and start all five services
docker compose up --build -d

# Check health of all services
docker compose ps

# Tail backend logs
docker compose logs -f backend

# Open a PostgreSQL shell
docker compose exec postgres psql -U postgres -d legal-ease

# Stop everything (preserve data)
docker compose down

# Stop and wipe all volumes (DESTROYS all data)
docker compose down -v
```

---

## 16. Environment Variables

Create `.env` at the repository root. Docker Compose loads it automatically.

| Variable | Description | Example |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | `legal-ease` |
| `POSTGRES_USER` | DB username | `postgres` |
| `POSTGRES_PASSWORD` | DB password | `strong_password` |
| `POSTGRES_PORT` | Host-exposed DB port | `5435` |
| `MINIO_ROOT_USER` | MinIO root username | `minioadmin` |
| `MINIO_ROOT_PASSWORD` | MinIO root password | `minioadmin` |
| `STORAGE_S3_BUCKET` | Bucket name | `legal-ease-docs` |
| `JWT_ACCESS_SECRET` | Access token signing key (≥32 chars) | `openssl rand -hex 32` |
| `JWT_REFRESH_SECRET` | Refresh token signing key (≥32 chars) | `openssl rand -hex 32` |
| `JWT_ISSUER` | JWT issuer claim | `legalease` |
| `JWT_ACCESS_EXP_MIN` | Access token TTL (minutes) | `30` |
| `JWT_REFRESH_EXP_MIN` | Refresh token TTL (minutes) | `1440` |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | Sender email address | `app@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password | `xxxx xxxx xxxx xxxx` |
| `DEEPSEEK_API_KEY` | DeepSeek AI API key | `sk-...` |
| `APP_SHARE_BASE_URL` | Public base URL for share links | `http://localhost:3000` |
| `NGINX_PORT` | Host port for Nginx (default: 80) | `80` |
| `BACKEND_PORT` | Host port for backend (local dev) | `9191` |

> **Gmail App Password:** Google Account → Security → 2-Step Verification → App passwords.
> Use a dedicated app password — never your main account password.

---

## 17. Database Migrations

Migrations are managed by **Liquibase** and applied automatically on application startup. The JPA is configured `ddl-auto: validate` — it never creates or modifies tables.

```
backend/src/main/resources/liquibase/changelogs/v1/
├── 001-create-sequences.xml              — global ID sequences
├── 002-create-role.xml
├── 003-create-users.xml
├── 004-create-user-role.xml
├── 005-create-verification-code.xml
├── 006-create-refresh-tokens.xml
├── 007-create-lawyer-application.xml
├── 008-insert-default-roles.xml          — seeds: USER, LAWYER, ADMIN
├── 009-insert-default-admins.xml         — seeds: default admin accounts
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

**Conventions:**
- Never modify existing changeset files
- New files: increment the `NNN` prefix; use format `NNN-description.xml`
- Always include a `<rollback>` block for every structural change
- One logical change per file

---

## 18. Testing

### Running Tests

```bash
# All backend tests (Testcontainers spins up PostgreSQL automatically)
cd backend && ./gradlew test

# Integration tests only
./gradlew test --tests "kz.legeal.ease.backend.api.*"

# PDF generation tests (no Docker required)
./gradlew test --tests "kz.legeal.ease.backend.PdfGenerationTest"

# Frontend: TypeScript check + Vite build
cd frontend && npm run build

# Frontend: ESLint
cd frontend && npm run lint
```

### Test Infrastructure

All API integration tests use:
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureMockMvc`
- **Testcontainers PostgreSQL** (singleton pattern — one container per test run)
- `@MockitoBean` for `StorageService` and `EmailSenderService`
- `StubAiService` (`@Profile("test")`) — returns no-op responses for all AI calls
- `BaseIntegrationTest` — shared container config, user factory, token helper, MockMvc builders

| Test class | What it covers |
|------------|---------------|
| `AuthApiTest` | Register, confirm, login, refresh, logout, reset, change-password — happy path + negative cases |
| `PublicApiTest` | Category list, document verification (VALID / INVALID / deleted) |
| `AdminApiTest` | Category CRUD, user block/unblock/revoke, lawyer applications, audit log search, rule toggles |
| `LawyerApiTest` | Template CRUD + publish, all rule types CRUD, clause library, lawyer document browse |
| `UserDocumentApiTest` | Full document lifecycle: create → validate → complete → archive → restore; download; share; versions |
| `SecurityApiTest` | Missing token → 401, invalid JWT → 401, IDOR (user B cannot access user A's documents), role isolation |

### PDF Tests

`PdfGenerationTest` (no Spring context required):
- Generates a narrative-mode PDF and a legacy-mode PDF
- Asserts `%PDF` magic bytes present
- Saves output files to `backend/test-document.pdf` and `backend/test-document-legacy.pdf` for manual inspection
- Verifies ZXing QR round-trip: encode URL → decode → same URL

---

## 19. Known Issues

| Severity | Issue |
|----------|-------|
| CRITICAL | CORS is configured to allow all origins with `allowCredentials = true` — must be restricted to specific origins before production deployment |
| CRITICAL | No rate limiting on `/open-api/auth/login` or `/open-api/auth/register` — vulnerable to brute-force and credential stuffing |
| HIGH | In-process rate limiter (`ConcurrentHashMap`) resets on restart — not safe for multi-instance deployments; replace with Redis + Bucket4j |
| HIGH | Missing pagination on several rule list endpoints — can return unbounded result sets |
| HIGH | IIN (Kazakhstan national ID) returned in `UserProfileDto` — sensitive PII that should be masked or omitted |
| MEDIUM | `LawyerRequestSearchCriteria` missing `@Valid` annotation in admin controller |
| MEDIUM | `fieldValues` map in `CreateDocumentRequest` has no size or key constraints |
| LOW | `/open-api/categories` not cached despite Caffeine being configured |
| LOW | No `ETag` or `Last-Modified` headers on document download responses |

---

## 20. Team

| Name | Role | Contact |
|------|------|---------|
| Daniyal | Backend Developer | orynbekdanial8@gmail.com |
| Yegazy | Backend Developer | yergazy.abdullayev@gmail.com |
| Olzhas | Backend Developer | olzhasergali56@gmail.com |

---

<div align="center">
  Built for legal accessibility in Kazakhstan
</div>
