package kz.legeal.ease.backend.request.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Map;

@Getter
@Schema(description = "Request to update a DRAFT document. All fields are optional.")
public class UpdateDocumentRequest {

    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    @Schema(description = "New document title (leave null to keep existing)", example = "Updated Lease Agreement")
    private String title;

    @Schema(description = "Replacement field values map. If provided, replaces all existing field values.")
    private Map<String, String> fieldValues;
}
