package kz.legeal.ease.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI-generated plain-language explanation of a completed document")
public record DocumentExplainResponse(
        @Schema(description = "What this document is and what it does") String summary,
        @Schema(description = "Key obligations and rights created by this document") String obligations,
        @Schema(description = "Important things to watch out for") String warnings,
        @Schema(description = "Next recommended steps for the user") String nextSteps
) {}
