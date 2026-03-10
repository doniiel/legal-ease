package kz.legeal.ease.backend.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name cannot be empty!")
    private String name;

    private String description;

    @NotNull(message = "Active status must be specified")
    private boolean active;
}
