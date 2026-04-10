package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.RequiredDocRuleDto;
import kz.legeal.ease.backend.request.RequiredDocRuleRequest;
import kz.legeal.ease.backend.service.RequiredDocRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/required-doc-rules")
@RequiredArgsConstructor
@Tag(name = "Required Document Rules (Lawyer)")
public class RequiredDocRuleController {

    private final RequiredDocRuleService requiredDocRuleService;

    @Operation(summary = "Get required-doc rules by template (LAWYER only)")
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<RequiredDocRuleDto>> getByTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(requiredDocRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create a required-doc rule (LAWYER only)")
    @PostMapping
    public ResponseEntity<RequiredDocRuleDto> create(@Valid @RequestBody RequiredDocRuleRequest request) {
        return ResponseEntity.ok(requiredDocRuleService.create(request));
    }

    @Operation(summary = "Update a required-doc rule (LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<RequiredDocRuleDto> update(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RequiredDocRuleRequest request
    ) {
        return ResponseEntity.ok(requiredDocRuleService.update(id, request));
    }

    @Operation(summary = "Delete a required-doc rule (LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Rule ID", required = true) @PathVariable Long id) {
        requiredDocRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
