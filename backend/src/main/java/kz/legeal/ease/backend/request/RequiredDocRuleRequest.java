package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.RuleConditionOperator;
import lombok.Getter;

@Getter
@Schema(description = "Request to create or update a required document rule")
public class RequiredDocRuleRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the template this rule belongs to", example = "1")
    private Long templateId;

    @NotBlank(message = "Required document title is required")
    @Size(max = 255, message = "Required document title must not exceed 255 characters")
    @Schema(description = "Title of the required supporting document", example = "Business License")
    private String requiredDocTitle;

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    @Schema(description = "Explanation of why the document is required")
    private String reason;

    /**
     * Optional. If null, the document is always required.
     * If set, the document is required only when the field satisfies the condition.
     */
    @Schema(description = "Field key to evaluate for conditional requirement (null = always required)")
    private String conditionFieldKey;

    @Schema(description = "Comparison operator used when conditionFieldKey is set")
    private RuleConditionOperator conditionOperator;

    @Size(max = 500, message = "Condition value must not exceed 500 characters")
    @Schema(description = "Value to compare the condition field against", example = "SELF_EMPLOYED")
    private String conditionValue;

    @Schema(description = "Whether the document is mandatory (default: true)")
    private boolean mandatory = true;
}
