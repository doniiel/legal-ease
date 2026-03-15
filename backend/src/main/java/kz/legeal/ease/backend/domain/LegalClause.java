package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * A reusable legal clause in the lawyer's knowledge base.
 * Lawyers can attach clauses to templates or use them as reference during document review.
 */
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "legal_clauses")
public class LegalClause extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(name = "legal_clause_seq", sequenceName = "legal_clause_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "legal_clause_seq")
    private Long id;

    /** Lawyer who owns this clause. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private User lawyer;

    /** Short descriptive title, e.g. "Standard Force Majeure Clause". */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Full clause text (may be many paragraphs). */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Optional comma-separated tags for search / filtering.
     * E.g. "force_majeure,liability,termination"
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /** Optional category association. Null = cross-category clause. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
