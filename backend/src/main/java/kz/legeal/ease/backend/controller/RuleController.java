package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.dto.*;
import kz.legeal.ease.backend.service.AdminRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Tag(name = "Rule Engine Management (Admin)")
public class RuleController {

    private final AdminRuleService adminRuleService;

    @Operation(summary = "List all validation rules (ADMIN only)")
    @GetMapping("/validation")
    public ResponseEntity<List<ValidationRuleDto>> getAllValidationRules() {
        return ResponseEntity.ok(adminRuleService.getAllValidationRules());
    }

    @Operation(summary = "Toggle validation rule active/inactive (ADMIN only)")
    @PatchMapping("/validation/{id}/toggle")
    public ResponseEntity<ValidationRuleDto> toggleValidationRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleValidationRule(id));
    }

    @Operation(summary = "List all risk rules (ADMIN only)")
    @GetMapping("/risk")
    public ResponseEntity<List<RiskRuleDto>> getAllRiskRules() {
        return ResponseEntity.ok(adminRuleService.getAllRiskRules());
    }

    @Operation(summary = "Toggle risk rule active/inactive (ADMIN only)")
    @PatchMapping("/risk/{id}/toggle")
    public ResponseEntity<RiskRuleDto> toggleRiskRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleRiskRule(id));
    }

    @Operation(summary = "List all matching rules (ADMIN only)")
    @GetMapping("/matching")
    public ResponseEntity<List<MatchingRuleDto>> getAllMatchingRules() {
        return ResponseEntity.ok(adminRuleService.getAllMatchingRules());
    }

    @Operation(summary = "Toggle matching rule active/inactive (ADMIN only)")
    @PatchMapping("/matching/{id}/toggle")
    public ResponseEntity<MatchingRuleDto> toggleMatchingRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleMatchingRule(id));
    }

    @Operation(summary = "List all conditional rules (ADMIN only)")
    @GetMapping("/conditional")
    public ResponseEntity<List<ConditionalRuleDto>> getAllConditionalRules() {
        return ResponseEntity.ok(adminRuleService.getAllConditionalRules());
    }

    @Operation(summary = "Toggle conditional rule active/inactive (ADMIN only)")
    @PatchMapping("/conditional/{id}/toggle")
    public ResponseEntity<ConditionalRuleDto> toggleConditionalRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleConditionalRule(id));
    }

    @Operation(summary = "List all required-doc rules (ADMIN only)")
    @GetMapping("/required-docs")
    public ResponseEntity<List<RequiredDocRuleDto>> getAllRequiredDocRules() {
        return ResponseEntity.ok(adminRuleService.getAllRequiredDocRules());
    }

    @Operation(summary = "Toggle required-doc rule active/inactive (ADMIN only)")
    @PatchMapping("/required-docs/{id}/toggle")
    public ResponseEntity<RequiredDocRuleDto> toggleRequiredDocRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleRequiredDocRule(id));
    }
}
