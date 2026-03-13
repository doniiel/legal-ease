package kz.legeal.ease.backend.request;

import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;

@Getter
public class ConditionalRuleRequest {

    private Long templateId;

    private String conditionFieldKey;

    private RuleConditionOperator operator;

    private String conditionValue;

    private String targetFieldKey;
}
