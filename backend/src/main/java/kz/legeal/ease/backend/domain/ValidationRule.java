package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "validation_rules")
public class ValidationRule extends AbstractAuditingEntity{

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "validation_rule_seq"
    )
    @SequenceGenerator(
            name = "validation_rule_seq",
            sequenceName = "validation_rule_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "field_label", nullable = false)
    private String fieldLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private RuleConditionOperator operator;

    @Column(name = "expected_value")
    private String expectedValue;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
