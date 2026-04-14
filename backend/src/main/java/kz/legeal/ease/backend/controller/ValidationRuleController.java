package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.ValidationRuleDto;
import kz.legeal.ease.backend.request.ValidationRuleRequest;
import kz.legeal.ease.backend.service.ValidationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validation-rules")
@RequiredArgsConstructor
@Tag(name = "Validation Rules (Lawyer)")
public class ValidationRuleController {

    private final ValidationRuleService validationRuleService;

    @Operation(summary = "Get validation rules by template (LAWYER only)")
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ValidationRuleDto>> getByTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(validationRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create validation rule (LAWYER only)")
    @PostMapping
    public ResponseEntity<ValidationRuleDto> create(@Valid @RequestBody ValidationRuleRequest request) {
        return ResponseEntity.ok(validationRuleService.create(request));
    }

    @Operation(summary = "Update validation rule (LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<ValidationRuleDto> update(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        return ResponseEntity.ok(validationRuleService.update(id, request));
    }

    @Operation(summary = "Delete validation rule (LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Rule ID", required = true) @PathVariable Long id) {
        validationRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
