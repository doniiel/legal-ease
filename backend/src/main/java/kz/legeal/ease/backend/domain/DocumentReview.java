package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.RiskLevel;
import lombok.*;

/**
 * A lawyer's review of a completed user document.
 * Lawyers can only review documents that were created from their own templates.
 */
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "document_reviews")
public class DocumentReview extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(name = "document_review_seq", sequenceName = "document_review_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_review_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /** The lawyer who performed the review. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private User lawyer;

    /** Free-form notes from the lawyer. */
    @Column(name = "notes", columnDefinition = "TEXT", nullable = false)
    private String notes;

    /** Overall risk assessment assigned by the lawyer. */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel;

    /** Whether the lawyer recommends approving / proceeding with this document. */
    @Column(name = "recommended", nullable = false)
    @Builder.Default
    private boolean recommended = true;
}
