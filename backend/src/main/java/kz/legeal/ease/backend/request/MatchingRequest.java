package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "Request to find matching document templates for given text")
public class MatchingRequest {

    @NotBlank(message = "Input text is required")
    @Size(max = 5000, message = "Input text must not exceed 5000 characters")
    @Schema(description = "Text describing the legal situation to match templates against")
    private String inputText;

    @Schema(description = "Optional category ID to narrow the search scope", example = "3")
    private Long categoryId;
}
