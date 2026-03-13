package kz.legeal.ease.backend.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateCategoryRequest {

    @NotBlank(message = "Category name cannot be empty!")
    private String name;

    private String description;
}
