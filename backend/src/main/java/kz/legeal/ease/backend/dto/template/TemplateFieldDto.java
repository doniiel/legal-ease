package kz.legeal.ease.backend.dto.template;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.enums.TemplateFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Template field definition")
public class TemplateFieldDto {

    @Schema(description = "Field ID", example = "1")
    private Long id;

    @Schema(description = "Field key used in document (snake_case)", example = "party_name")
    private String fieldKey;

    @Schema(description = "Human readable label", example = "Имя стороны")
    private String label;

    @Schema(description = "Field type", example = "TEXT")
    private TemplateFieldType fieldType;

    @Schema(description = "Whether this field is required", example = "true")
    private boolean required;

    @Schema(description = "Display order", example = "1")
    private int orderNum;
}
