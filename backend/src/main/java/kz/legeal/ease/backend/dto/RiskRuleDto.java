package kz.legeal.ease.backend.dto;

import kz.legeal.ease.backend.enums.RiskLevel;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRuleDto {

    private Long id;

    private Long templateId;

    private String templateTitle;

    private String ruleCode;

    private String fieldKey;

    private RuleConditionOperator operator;

    private String expectedValue;

    private String riskMessage;

    private RiskLevel riskLevel;

    private boolean active;
}
