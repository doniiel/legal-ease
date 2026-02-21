package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Reject lawyer request payload")
public class RejectLawyerRequest {

    @NotBlank
    @Schema(description = "Reason for rejection",
            example = "License number is invalid")
    private String reason;
}
