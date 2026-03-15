package kz.legeal.ease.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Summary of a single immutable PDF version stored in S3. */
@Schema(description = "Immutable PDF version record — created each time a document is completed")
public record DocumentVersionDto(
        @Schema(description = "Version record ID") Long id,
        @Schema(description = "Version number (1-based)", example = "2") int version,
        @Schema(description = "S3 object key for this version's PDF") String s3ObjectKey,
        @Schema(description = "When this version was created") LocalDateTime createdDate,
        @Schema(description = "Email of the user who triggered the completion") String createdBy
) {}
