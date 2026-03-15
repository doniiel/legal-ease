package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create or update a template matching rule")
public class MatchingRuleRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the template this matching rule belongs to", example = "1")
    private Long templateId;

    @NotNull(message = "Category ID is required")
    @Schema(description = "ID of the category to match", example = "3")
    private Long categoryId;

    @NotBlank(message = "Keywords are required")
    @Size(max = 2000, message = "Keywords must not exceed 2000 characters")
    @Schema(description = "Comma-separated keywords that trigger this match", example = "аренда, квартира, жилье")
    private String keywords;

    @Min(value = 0, message = "Base score must be at least 0")
    @Max(value = 100, message = "Base score must not exceed 100")
    @Schema(description = "Base score for this match (0–100). Defaults to 50 if not provided.", example = "75")
    private Integer baseScore;
}
