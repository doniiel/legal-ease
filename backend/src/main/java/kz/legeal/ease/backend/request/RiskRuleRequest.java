package kz.legeal.ease.backend.request;

import kz.legeal.ease.backend.enums.RiskLevel;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRuleRequest {

    private Long templateId;

    private String ruleCode;

    private String fieldKey;

    private RuleConditionOperator operator;

    private String expectedValue;

    private String riskMessage;

    private RiskLevel riskLevel;
}
