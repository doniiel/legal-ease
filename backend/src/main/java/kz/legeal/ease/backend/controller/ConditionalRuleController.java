package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.ConditionalRuleDto;
import kz.legeal.ease.backend.request.ConditionalRuleRequest;
import kz.legeal.ease.backend.service.ConditionalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conditional-rules")
@RequiredArgsConstructor
@Tag(name = "Conditional Rules (Lawyer)")
public class ConditionalRuleController {

    private final ConditionalRuleService conditionalRuleService;

    @Operation(summary = "Get conditional rules by template (LAWYER only)")
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ConditionalRuleDto>> getByTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(conditionalRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create conditional rule (LAWYER only)")
    @PostMapping
    public ResponseEntity<ConditionalRuleDto> create(@Valid @RequestBody ConditionalRuleRequest request) {
        return ResponseEntity.ok(conditionalRuleService.create(request));
    }

    @Operation(summary = "Update conditional rule (LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<ConditionalRuleDto> update(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ConditionalRuleRequest request
    ) {
        return ResponseEntity.ok(conditionalRuleService.update(id, request));
    }

    @Operation(summary = "Delete conditional rule (LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Rule ID", required = true) @PathVariable Long id) {
        conditionalRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
