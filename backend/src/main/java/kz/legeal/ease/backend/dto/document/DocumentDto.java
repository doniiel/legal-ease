package kz.legeal.ease.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.enums.DocumentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "Full document representation including field values and rule engine feedback")
public class DocumentDto {

    @Schema(description = "Unique document identifier", example = "42")
    private Long id;

    @Schema(description = "Document title", example = "Lease Agreement 2026")
    private String title;

    @Schema(description = "ID of the template used to create this document", example = "1")
    private Long templateId;

    @Schema(description = "Title of the source template")
    private String templateTitle;

    @Schema(description = "Category name of the source template")
    private String categoryName;

    @Schema(description = "Current lifecycle status: DRAFT or COMPLETED")
    private DocumentStatus status;

    @Schema(description = "Map of field key → value pairs filled in by the user")
    private List<DocumentFieldValueDto> fieldValues;

    @Schema(description = "List of required field keys that have not been filled in yet")
    private List<String> missingRequiredFields = new ArrayList<>();

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedDate;

    @Schema(description = "S3 object key of the generated PDF (non-null only for COMPLETED documents)")
    private String s3ObjectKey;
}
