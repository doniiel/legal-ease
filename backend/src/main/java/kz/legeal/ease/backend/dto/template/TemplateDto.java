package kz.legeal.ease.backend.dto.template;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.dto.BaseUserDto;
import kz.legeal.ease.backend.enums.TemplateStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Full template DTO (for lawyer)")
public class TemplateDto {

    @Schema(description = "Template ID", example = "1")
    private Long id;

    @Schema(description = "Template title", example = "Договор купли-продажи")
    private String title;

    @Schema(description = "Template description")
    private String description;

    @Schema(description = "Category", example = "CIVIL")
    private Category category;

    @Schema(description = "Template status", example = "DRAFT")
    private TemplateStatus status;

    @Schema(description = "Lawyer who created the template")
    private BaseUserDto lawyer;

    @Schema(description = "Template fields")
    private List<TemplateFieldDto> fields;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Last updated date")
    private LocalDateTime updatedDate;
}
