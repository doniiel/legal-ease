package kz.legeal.ease.backend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.dto.template.TemplatePreviewDto;
import kz.legeal.ease.backend.service.tempate.TemplatePublicQueryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/templates")
@PreAuthorize("hasRole('USER')")
@Tag(
        name = "User - Public Templates",
        description = "API for browsing and viewing published document templates available to users."
)
@Validated
@RequiredArgsConstructor
public class UserTemplateController {

    private final TemplatePublicQueryService publicQueryService;

    @Operation(
            summary = "Get published templates (paginated)",
            description = "Returns a paginated list of templates with PUBLISHED status. " +
                    "Optionally allows filtering by category. " +
                    "Results are sorted by creation date in descending order by default."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Published templates retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    public ResponseEntity<Page<TemplatePreviewDto>> getPublished(
            @Parameter(
                    description = "Optional category filter. If not provided, all categories are returned."
            )
            @RequestParam(required = false) Category category,
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(publicQueryService.getPublishedTemplates(category, pageable));
    }

    @Operation(
            summary = "Get published template by ID",
            description = "Returns detailed information about a template by its ID. " +
                    "Only templates with PUBLISHED status are accessible to users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "404", description = "Template not found or not published"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getById(
            @Parameter(
                    description = "Unique identifier of the template",
                    required = true
            )
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(publicQueryService.getPublishedTemplateById(id));
    }
}