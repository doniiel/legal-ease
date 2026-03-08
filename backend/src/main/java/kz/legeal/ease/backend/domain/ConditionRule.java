package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "condition_rules")
public class ConditionRule {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "condition_rule_seq"
    )
    @SequenceGenerator(
            name = "condition_rule_seq",
            sequenceName = "condition_rule_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "condition_field_key", nullable = false)
    private String conditionFieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private RuleConditionOperator operator;

    @Column(name = "condition_value", nullable = false)
    private String conditionValue;

    @Column(name = "target_field_key", nullable = false)
    private String targetFieldKey;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

}
