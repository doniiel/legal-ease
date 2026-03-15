package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.RiskLevel;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create or update a risk detection rule")
public class RiskRuleRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the template this risk rule belongs to", example = "1")
    private Long templateId;

    @NotBlank(message = "Rule code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Rule code must be uppercase letters, digits, or underscores")
    @Size(max = 100, message = "Rule code must not exceed 100 characters")
    @Schema(description = "Unique code identifying this risk rule", example = "RENT_HIGH_RISK")
    private String ruleCode;

    @NotBlank(message = "Field key is required")
    @Schema(description = "The field key to evaluate for risk", example = "monthly_rent")
    private String fieldKey;

    @NotNull(message = "Operator is required")
    @Schema(description = "Comparison operator for the risk condition")
    private RuleConditionOperator operator;

    @Size(max = 500, message = "Expected value must not exceed 500 characters")
    @Schema(description = "Threshold value to compare the field against", example = "500000")
    private String expectedValue;

    @NotBlank(message = "Risk message is required")
    @Size(max = 1000, message = "Risk message must not exceed 1000 characters")
    @Schema(description = "Message describing the detected risk")
    private String riskMessage;

    @NotNull(message = "Risk level is required")
    @Schema(description = "Severity level of the risk")
    private RiskLevel riskLevel;
}
