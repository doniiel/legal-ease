package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.request.category.CreateCategoryRequest;
import kz.legeal.ease.backend.request.category.UpdateCategoryRequest;
import kz.legeal.ease.backend.service.CategoryService;
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
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Admin - Category Management",
        description = "CRUD operations for managing document categories"
)
@Validated
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Create a new category",
            description = "Creates a new document category. Category name must be unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Category with the same name already exists")
    })
    @PostMapping
    public ResponseEntity<CategoryDto> create(
            @RequestBody @Valid CreateCategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(request));
    }

    @Operation(
            summary = "Get all categories (paginated)",
            description = "Returns a paginated list of all categories (active and inactive)"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated categories list")
    @GetMapping
    public ResponseEntity<Page<CategoryDto>> getAll(
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    @Operation(
            summary = "Get category by ID",
            description = "Returns category details by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(
            @Parameter(
                    description = "Unique identifier of the category",
                    required = true,
                    example = "1"
            )
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @Operation(
            summary = "Update category",
            description = "Updates an existing category by ID. Category name must remain unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category name already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @Parameter(
                    description = "Unique identifier of the category",
                    required = true,
                    example = "1"
            )
            @NotNull @PathVariable Long id,
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @Operation(
            summary = "Deactivate category",
            description = "Soft deletes (deactivates) a category. Deletion is not allowed if related templates exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category successfully deactivated"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Cannot deactivate category due to existing related templates")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "Unique identifier of the category",
                    required = true,
                    example = "1"
            )
            @NotNull @PathVariable Long id
    ) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
