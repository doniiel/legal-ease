package kz.legeal.ease.backend.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.legeal.ease.backend.enums.TemplateFieldType;
import lombok.Getter;

@Getter
public class TemplateFieldRequest {

    @NotBlank(message = "Field key is required")
    @Size(min = 2, max = 100, message = "Field key must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[a-z][a-z0-9_]*$",
            message = "Field key must be snake_case: lowercase letters, digits, underscores (e.g. party_name)"
    )
    private String fieldKey;

    @NotBlank(message = "Label is required")
    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String label;

    @NotNull(message = "Field type is required")
    private TemplateFieldType fieldType;

    private boolean required = false;

    private int orderNum = 0;
}