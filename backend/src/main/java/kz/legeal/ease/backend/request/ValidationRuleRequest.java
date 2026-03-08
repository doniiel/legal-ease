package kz.legeal.ease.backend.request;

import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;

@Getter
public class ValidationRuleRequest {

    private Long templateId;

    private String fieldKey;

    private String fieldLabel;

    private RuleConditionOperator operator;

    private String expectedValue;

    private String errorMessage;
}
