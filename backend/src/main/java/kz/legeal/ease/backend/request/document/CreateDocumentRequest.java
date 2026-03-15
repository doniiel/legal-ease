package kz.legeal.ease.backend.request.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Map;

@Getter
@Schema(description = "Request to create a new document from a template")
public class CreateDocumentRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the published template to create the document from", example = "1")
    private Long templateId;

    @NotBlank(message = "Document title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    @Schema(description = "Title of the document", example = "Lease Agreement — Almaty 2026")
    private String title;

    @Schema(description = "Map of field keys to their values. Keys must match the template's field keys.")
    private Map<String, String> fieldValues;
}
