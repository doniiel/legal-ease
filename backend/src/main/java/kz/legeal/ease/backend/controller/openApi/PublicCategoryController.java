package kz.legeal.ease.backend.controller.openApi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/open-api/categories")
@Tag(
        name = "Public - Categories",
        description = "Public endpoint for retrieving active categories"
)
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get all active categories",
            description = "Returns a list of all active categories available for public access"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of active categories"
    )
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }
}
