package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@Tag(name = "Templates - Public Browse")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplatePublicQueryService publicQueryService;

    @Operation(summary = "Get published templates (paginated)")
    @GetMapping
    public ResponseEntity<Page<TemplatePreviewDto>> getPublished(
            @Parameter(description = "Optional category filter") @RequestParam(required = false) Long categoryId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(publicQueryService.getPublishedTemplates(categoryId, pageable));
    }

    @Operation(summary = "Get published template by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getById(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(publicQueryService.getPublishedTemplateById(id));
    }
}
