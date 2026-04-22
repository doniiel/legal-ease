package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.ReviewStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to submit a lawyer review of a user document")
public class DocumentReviewRequest {

    @NotBlank
    @Size(max = 5000)
    @Schema(description = "Lawyer's comment or observations about the document", required = true)
    private String comment;

    @NotNull
    @Schema(description = "Review decision: APPROVED, REJECTED, or NEEDS_REVISION", required = true)
    private ReviewStatus status;
}
