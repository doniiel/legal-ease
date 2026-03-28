package kz.legeal.ease.backend.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TemplateRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Optional template body with {@code {{field_key}}} placeholders.
     * Example:
     * <pre>
     *   Настоящий договор заключён {{date}} между {{client_name}} и {{company_name}}.
     *   Предмет договора: {{subject}}.
     * </pre>
     * Leave blank to use the legacy field-table PDF layout.
     */
    @Size(max = 100_000, message = "Template body must not exceed 100 000 characters")
    private String body;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Valid
    private List<TemplateFieldRequest> fields = new ArrayList<>();
}