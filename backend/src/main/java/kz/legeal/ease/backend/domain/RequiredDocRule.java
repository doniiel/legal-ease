package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.*;

/**
 * Rule that defines which supporting documents a user must (or should) provide
 * together with the main document derived from the given template.
 *
 * <p>Two modes:
 * <ul>
 *   <li><b>Unconditional</b> – {@code conditionFieldKey} is {@code null}:
 *       the document is always required regardless of field values.</li>
 *   <li><b>Conditional</b> – {@code conditionFieldKey} is set:
 *       the document is required only when
 *       {@code fieldValues[conditionFieldKey] (conditionOperator) conditionValue} evaluates to
 *       {@code true}.</li>
 * </ul>
 *
 * <p>Lawyers create these rules; admins can toggle them on/off.
 * The {@link kz.legeal.ease.backend.service.rule.handler.RequiredDocsHandler}
 * evaluates them at runtime and populates
 * {@link kz.legeal.ease.backend.service.rule.chain.RuleChainContext#getRequiredDocuments()}.
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "required_doc_rules")
public class RequiredDocRule extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "required_doc_rule_seq"
    )
    @SequenceGenerator(
            name = "required_doc_rule_seq",
            sequenceName = "required_doc_rule_seq",
            allocationSize = 1
    )
    private Long id;

    /** The template this rule belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    /** Human-readable name of the required supporting document (shown to users). */
    @Column(name = "required_doc_title", nullable = false, length = 255)
    private String requiredDocTitle;

    /**
     * Base explanation of why this document is needed.
     * Will be enriched with an AI-generated explanation by
     * {@link kz.legeal.ease.backend.service.rule.handler.AIDocsExplainerHandler}.
     */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * Optional field key used for conditional evaluation.
     * If {@code null}, the rule fires unconditionally.
     */
    @Column(name = "condition_field_key", length = 100)
    private String conditionFieldKey;

    /** Operator applied to {@code fieldValues[conditionFieldKey]}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator", length = 50)
    private RuleConditionOperator conditionOperator;

    /** Right-hand side value for the condition check. */
    @Column(name = "condition_value", length = 500)
    private String conditionValue;

    /**
     * {@code true}  → hard requirement; document is mandatory.
     * {@code false} → advisory; recommended but not blocking.
     */
    @Column(name = "mandatory", nullable = false)
    @Builder.Default
    private boolean mandatory = true;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
