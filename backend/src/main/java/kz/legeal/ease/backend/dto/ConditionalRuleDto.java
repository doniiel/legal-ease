package kz.legeal.ease.backend.dto;

import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionalRuleDto {

    private Long id;

    private Long templateId;

    private String templateTitle;

    private String conditionalFieldKey;

    private RuleConditionOperator operator;

    private String conditionalValue;

    private String targetFieldKey;

    private boolean active;
}
