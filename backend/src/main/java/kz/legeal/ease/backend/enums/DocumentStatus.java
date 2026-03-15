package kz.legeal.ease.backend.enums;

/**
 * Document lifecycle states:
 *
 * <pre>
 * DRAFT ──► VALIDATED ──► COMPLETED ──► ARCHIVED
 *  ▲                           │
 *  └──────── restore ──────────┘
 * </pre>
 *
 * DRAFT     — created, being edited; rule engine not yet invoked.
 * VALIDATED — rule engine passed; all validation rules cleared, ready for PDF generation.
 * COMPLETED — PDF generated and stored in S3.
 * ARCHIVED  — soft-archived by the user; hidden from default listing but preserved.
 */
public enum DocumentStatus {
    DRAFT,
    VALIDATED,
    COMPLETED,
    ARCHIVED
}
