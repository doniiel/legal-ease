package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.request.TemplateRequest;
import kz.legeal.ease.backend.service.tempate.TemplateCommandService;
import kz.legeal.ease.backend.service.tempate.TemplateQueryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lawyer/templates")
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Template Management",
        description = "API for managing document templates created by lawyers. " +
                "Allows creating, updating, publishing, retrieving, and soft-deleting templates."
)
@Validated
@RequiredArgsConstructor
public class LawyerTemplateController {

    private final TemplateCommandService commandService;
    private final TemplateQueryService queryService;

    @Operation(
            summary = "Create a new template (DRAFT status)",
            description = "Creates a new document template in DRAFT status. " +
                    "Only users with LAWYER role are allowed to perform this operation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    public ResponseEntity<TemplateDto> create(
            @RequestBody @Valid TemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.create(request));
    }

    @Operation(
            summary = "Get my templates (paginated)",
            description = "Returns a paginated list of templates created by the currently authenticated lawyer. " +
                    "Templates are sorted by creation date in descending order by default."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    public ResponseEntity<Page<TemplateDto>> getMyTemplates(
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(queryService.getMyTemplates(pageable));
    }

    @Operation(
            summary = "Get template by ID",
            description = "Returns a template by its ID. " +
                    "The template must belong to the currently authenticated lawyer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "403", description = "Template does not belong to the current user"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getById(
            @Parameter(description = "Unique identifier of the template", required = true)
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(queryService.getMyTemplateById(id));
    }

    @Operation(
            summary = "Update template (DRAFT only)",
            description = "Updates an existing template. " +
                    "Only templates in DRAFT status can be updated. " +
                    "The template must belong to the current lawyer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @ApiResponse(responseCode = "403", description = "Template does not belong to the user or is already published"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> update(
            @Parameter(description = "Unique identifier of the template", required = true)
            @NotNull @PathVariable Long id,
            @RequestBody @Valid TemplateRequest request
    ) {
        return ResponseEntity.ok(commandService.update(id, request));
    }

    @Operation(
            summary = "Publish template (DRAFT → PUBLISHED)",
            description = "Changes template status from DRAFT to PUBLISHED. " +
                    "Once published, the template cannot be modified or deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template successfully published"),
            @ApiResponse(responseCode = "403", description = "Template does not belong to the user"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "409", description = "Template is already published")
    })
    @PostMapping("/{id}/publish")
    public ResponseEntity<TemplateDto> publish(
            @Parameter(description = "Unique identifier of the template", required = true)
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(commandService.publish(id));
    }

    @Operation(
            summary = "Delete template (DRAFT only, soft delete)",
            description = "Performs a soft delete of a template. " +
                    "Only templates in DRAFT status can be deleted. " +
                    "The template must belong to the current lawyer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Template successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Template does not belong to the user or is already published"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the template", required = true)
            @NotNull @PathVariable Long id
    ) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}