package kz.legeal.ease.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Category DTO")
public class CategoryDto {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Гражданское право")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Is active", example = "true")
    private boolean active;
}
