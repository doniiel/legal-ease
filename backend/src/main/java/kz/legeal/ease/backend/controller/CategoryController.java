package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management (Admin)")
@Validated
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category (ADMIN only)")
    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @Operation(summary = "Get all categories (ADMIN only)")
    @GetMapping
    public ResponseEntity<Page<CategoryDto>> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    @Operation(summary = "Get category by ID (ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(
            @Parameter(description = "Category ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @Operation(summary = "Update category (ADMIN only)")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @Parameter(description = "Category ID", required = true) @NotNull @PathVariable Long id,
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @Operation(summary = "Deactivate category (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category ID", required = true) @NotNull @PathVariable Long id
    ) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
