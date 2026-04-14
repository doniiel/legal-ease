package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-templates")
@Tag(name = "My Templates (Lawyer)")
@Validated
@RequiredArgsConstructor
public class MyTemplateController {

    private final TemplateCommandService commandService;
    private final TemplateQueryService queryService;

    @Operation(summary = "Create a new template (LAWYER only)")
    @PostMapping
    public ResponseEntity<TemplateDto> create(@RequestBody @Valid TemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(request));
    }

    @Operation(summary = "Get my templates (LAWYER only)")
    @GetMapping
    public ResponseEntity<Page<TemplateDto>> getMyTemplates(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(queryService.getMyTemplates(pageable));
    }

    @Operation(summary = "Get my template by ID (LAWYER only)")
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getById(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(queryService.getMyTemplateById(id));
    }

    @Operation(summary = "Update template (DRAFT only, LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> update(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long id,
            @RequestBody @Valid TemplateRequest request
    ) {
        return ResponseEntity.ok(commandService.update(id, request));
    }

    @Operation(summary = "Publish template (DRAFT → PUBLISHED, LAWYER only)")
    @PostMapping("/{id}/publish")
    public ResponseEntity<TemplateDto> publish(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(commandService.publish(id));
    }

    @Operation(summary = "Delete template (DRAFT only, LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long id
    ) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
