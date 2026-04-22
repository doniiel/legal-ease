package kz.legeal.ease.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.enums.ReviewStatus;

import java.time.LocalDateTime;

@Schema(description = "A lawyer's review of a user document")
public record DocumentReviewDto(
        @Schema(description = "Review ID") Long id,
        @Schema(description = "ID of the reviewed document") Long documentId,
        @Schema(description = "Document title") String documentTitle,
        @Schema(description = "Brief info about the reviewing lawyer") LawyerInfo lawyerInfo,
        @Schema(description = "Lawyer's comment") String comment,
        @Schema(description = "Review decision: APPROVED, REJECTED, NEEDS_REVISION") ReviewStatus status,
        @Schema(description = "When the review was submitted") LocalDateTime createdDate
) {
    public record LawyerInfo(Long id, String fio, String email) {}
}
