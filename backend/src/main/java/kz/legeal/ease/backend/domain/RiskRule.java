package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.RiskLevel;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "risk_rules")
public class RiskRule extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "risk_rule_seq"
    )
    @SequenceGenerator(
            name = "risk_rule_seq",
            sequenceName = "risk_rule_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "rule_code", nullable = false, unique = true)
    private String ruleCode;

    @Column(name = "field_key")
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private RuleConditionOperator operator;

    @Column(name = "expected_value")
    private String expectedValue;

    @Column(name = "risk_message", nullable = false, columnDefinition = "TEXT")
    private String riskMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

}
