package kz.legeal.ease.backend.dto;

import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequiredDocRuleDto {

    private Long id;

    private Long templateId;

    private String templateTitle;

    private String requiredDocTitle;

    private String reason;

    /** Null means the document is always required (unconditional). */
    private String conditionFieldKey;

    private RuleConditionOperator conditionOperator;

    private String conditionValue;

    private boolean mandatory;

    private boolean active;
}
