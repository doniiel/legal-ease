package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.RiskLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to submit a lawyer review of a user document")
public class DocumentReviewRequest {

    @NotBlank
    @Size(min = 10, max = 5000)
    @Schema(description = "Lawyer's written notes or observations about the document", required = true)
    private String notes;

    @NotNull
    @Schema(description = "Overall risk level the lawyer assigns to this document", required = true)
    private RiskLevel riskLevel;

    @Schema(description = "Whether the lawyer recommends proceeding with this document", defaultValue = "true")
    private boolean recommended = true;
}
