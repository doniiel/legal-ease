package kz.legeal.ease.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.enums.RiskLevel;

import java.time.LocalDateTime;

@Schema(description = "A lawyer's written review of a user document")
public record DocumentReviewDto(
        @Schema(description = "Review ID") Long id,
        @Schema(description = "ID of the reviewed document") Long documentId,
        @Schema(description = "Document title") String documentTitle,
        @Schema(description = "ID of the reviewing lawyer") Long lawyerId,
        @Schema(description = "Lawyer's email / display name") String lawyerEmail,
        @Schema(description = "Lawyer's notes") String notes,
        @Schema(description = "Lawyer's risk assessment") RiskLevel riskLevel,
        @Schema(description = "Whether the lawyer recommends proceeding") boolean recommended,
        @Schema(description = "When the review was submitted") LocalDateTime createdDate
) {}
