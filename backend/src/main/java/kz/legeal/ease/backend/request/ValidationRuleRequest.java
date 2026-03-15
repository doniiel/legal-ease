package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;

@Getter
@Schema(description = "Request to create or update a validation rule on a template field")
public class ValidationRuleRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the template this rule belongs to", example = "1")
    private Long templateId;

    @NotBlank(message = "Field key is required")
    @Schema(description = "The field key this rule applies to", example = "rental_date")
    private String fieldKey;

    @NotBlank(message = "Field label is required")
    @Schema(description = "Human-readable field label shown in error messages", example = "Rental Date")
    private String fieldLabel;

    @NotNull(message = "Operator is required")
    @Schema(description = "Comparison operator")
    private RuleConditionOperator operator;

    @Size(max = 500, message = "Expected value must not exceed 500 characters")
    @Schema(description = "The value to compare against", example = "2026-01-01")
    private String expectedValue;

    @NotBlank(message = "Error message is required")
    @Size(max = 1000, message = "Error message must not exceed 1000 characters")
    @Schema(description = "Message shown to the user when this rule is violated")
    private String errorMessage;
}
