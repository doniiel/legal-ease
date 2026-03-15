package kz.legeal.ease.backend.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create or update a legal clause in the knowledge base")
public class LegalClauseRequest {

    @NotBlank
    @Size(min = 3, max = 255)
    @Schema(description = "Short descriptive title, e.g. 'Standard Force Majeure Clause'", required = true)
    private String title;

    @NotBlank
    @Size(min = 10, max = 50000)
    @Schema(description = "Full clause text", required = true)
    private String content;

    @Size(max = 500)
    @Schema(description = "Optional comma-separated tags, e.g. 'force_majeure,liability'")
    private String tags;

    @Schema(description = "Optional category ID to associate this clause with")
    private Long categoryId;
}
