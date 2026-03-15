package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;

@Getter
@Schema(description = "Request to create or update a conditional field rule")
public class ConditionalRuleRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the template this conditional rule belongs to", example = "1")
    private Long templateId;

    @NotBlank(message = "Condition field key is required")
    @Schema(description = "The field key whose value is evaluated", example = "employment_type")
    private String conditionFieldKey;

    @NotNull(message = "Operator is required")
    @Schema(description = "Comparison operator for the condition")
    private RuleConditionOperator operator;

    @NotBlank(message = "Condition value is required")
    @Size(max = 500, message = "Condition value must not exceed 500 characters")
    @Schema(description = "Value to compare the condition field against", example = "SELF_EMPLOYED")
    private String conditionValue;

    @NotBlank(message = "Target field key is required")
    @Schema(description = "The field that becomes required when the condition is true", example = "business_license_number")
    private String targetFieldKey;
}
