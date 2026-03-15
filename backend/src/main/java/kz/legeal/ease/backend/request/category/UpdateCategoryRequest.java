package kz.legeal.ease.backend.request.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "Request to update an existing document category")
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name cannot be empty")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(description = "Updated category name", example = "Rental Agreements")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Updated category description")
    private String description;

    @NotNull(message = "Active status must be specified")
    @Schema(description = "Whether the category is active and visible to users")
    private boolean active;
}
