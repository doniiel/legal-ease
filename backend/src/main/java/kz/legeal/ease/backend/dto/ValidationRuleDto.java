package kz.legeal.ease.backend.dto;

import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationRuleDto {

    private Long id;

    private Long templateId;

    private String templateTitle;

    private String fieldKey;

    private String fieldLabel;

    private RuleConditionOperator operator;

    private String expectedValue;

    private String errorMessage;

    private boolean active;
}
