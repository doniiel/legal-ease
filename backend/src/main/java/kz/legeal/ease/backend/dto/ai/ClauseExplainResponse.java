package kz.legeal.ease.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI-generated plain-language explanation of a legal clause")
public record ClauseExplainResponse(
        @Schema(description = "Plain-language explanation of the submitted legal clause") String explanation
) {}
