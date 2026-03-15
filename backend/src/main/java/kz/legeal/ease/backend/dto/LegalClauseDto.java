package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "A reusable legal clause in the lawyer's knowledge base")
public record LegalClauseDto(
        @Schema(description = "Clause ID") Long id,
        @Schema(description = "Clause title") String title,
        @Schema(description = "Full clause text") String content,
        @Schema(description = "Comma-separated tags") String tags,
        @Schema(description = "Associated category ID, or null") Long categoryId,
        @Schema(description = "Associated category name, or null") String categoryName,
        @Schema(description = "Whether the clause is active") boolean active,
        @Schema(description = "When the clause was created") LocalDateTime createdDate,
        @Schema(description = "When the clause was last updated") LocalDateTime updatedDate
) {}
