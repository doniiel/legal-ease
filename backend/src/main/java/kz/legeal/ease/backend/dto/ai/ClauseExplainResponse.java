package kz.legeal.ease.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI-generated plain-language explanation of a legal clause")
public record ClauseExplainResponse(
        @Schema(description = "What this clause means in plain language") String explanation,
        @Schema(description = "Simplified one-sentence version of the clause") String simplifiedText,
        @Schema(description = "List of risks or concerns in this clause") List<String> risks,
        @Schema(description = "Recommendations for the user") List<String> recommendations
) {}
