package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.RiskRuleDto;
import kz.legeal.ease.backend.request.RiskRuleRequest;
import kz.legeal.ease.backend.service.RiskRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-rules")
@RequiredArgsConstructor
@Tag(name = "Risk Rules (Lawyer)")
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    @Operation(summary = "Get risk rules by template (LAWYER only)")
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<RiskRuleDto>> getByTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(riskRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create risk rule (LAWYER only)")
    @PostMapping
    public ResponseEntity<RiskRuleDto> create(@Valid @RequestBody RiskRuleRequest request) {
        return ResponseEntity.ok(riskRuleService.create(request));
    }

    @Operation(summary = "Update risk rule (LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<RiskRuleDto> update(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RiskRuleRequest request
    ) {
        return ResponseEntity.ok(riskRuleService.update(id, request));
    }

    @Operation(summary = "Delete risk rule (LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Rule ID", required = true) @PathVariable Long id) {
        riskRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
