package kz.legeal.ease.backend.dto.template;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.legeal.ease.backend.dto.CategoryDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Template preview DTO (for user listing — no fields, no lawyer details)")
public class TemplatePreviewDto {

    @Schema(description = "Template ID", example = "1")
    private Long id;

    @Schema(description = "Template title", example = "Договор купли-продажи")
    private String title;

    @Schema(description = "Template description")
    private String description;

    @Schema(description = "Category", example = "CIVIL")
    private CategoryDto category;

    @Schema(description = "Lawyer name", example = "Иванов Иван")
    private String lawyerName;

    @Schema(description = "Number of fields", example = "5")
    private int fieldCount;

    @Schema(description = "Published date")
    private LocalDateTime createdDate;
}