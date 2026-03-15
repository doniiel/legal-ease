package kz.legeal.ease.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.dto.*;
import kz.legeal.ease.backend.service.AdminRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only Rule Engine management.
 *
 * <p>Admins can view all rules across all templates and toggle any rule on/off.
 * Every toggle is audited automatically (updatedBy / updatedDate via AbstractAuditingEntity).
 *
 * <p>Admins do NOT create or edit rule content — that is the lawyer's responsibility.
 */
@RestController
@RequestMapping("/api/admin/rules")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(
        name = "Admin - Rule Engine Management",
        description = "View all rules and toggle them active/inactive globally"
)
public class AdminRuleController {

    private final AdminRuleService adminRuleService;

    // ── Validation Rules ──────────────────────────────────────────────────────

    @Operation(summary = "List all validation rules (across all templates)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved")
    @GetMapping("/validation")
    public ResponseEntity<List<ValidationRuleDto>> getAllValidationRules() {
        return ResponseEntity.ok(adminRuleService.getAllValidationRules());
    }

    @Operation(
            summary = "Toggle validation rule active/inactive",
            description = "Flips the active flag. The change takes effect immediately for all new document completions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule toggled, updated rule returned"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/validation/{id}/toggle")
    public ResponseEntity<ValidationRuleDto> toggleValidationRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleValidationRule(id));
    }

    // ── Risk Rules ────────────────────────────────────────────────────────────

    @Operation(summary = "List all risk rules (across all templates)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved")
    @GetMapping("/risk")
    public ResponseEntity<List<RiskRuleDto>> getAllRiskRules() {
        return ResponseEntity.ok(adminRuleService.getAllRiskRules());
    }

    @Operation(
            summary = "Toggle risk rule active/inactive",
            description = "Flips the active flag. Users will immediately stop/start seeing this risk warning."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule toggled"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/risk/{id}/toggle")
    public ResponseEntity<RiskRuleDto> toggleRiskRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleRiskRule(id));
    }

    // ── Matching Rules ────────────────────────────────────────────────────────

    @Operation(summary = "List all matching rules (across all templates)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved")
    @GetMapping("/matching")
    public ResponseEntity<List<MatchingRuleDto>> getAllMatchingRules() {
        return ResponseEntity.ok(adminRuleService.getAllMatchingRules());
    }

    @Operation(summary = "Toggle matching rule active/inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule toggled"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/matching/{id}/toggle")
    public ResponseEntity<MatchingRuleDto> toggleMatchingRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleMatchingRule(id));
    }

    // ── Conditional Rules ─────────────────────────────────────────────────────

    @Operation(summary = "List all conditional rules (across all templates)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved")
    @GetMapping("/conditional")
    public ResponseEntity<List<ConditionalRuleDto>> getAllConditionalRules() {
        return ResponseEntity.ok(adminRuleService.getAllConditionalRules());
    }

    @Operation(summary = "Toggle conditional rule active/inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule toggled"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/conditional/{id}/toggle")
    public ResponseEntity<ConditionalRuleDto> toggleConditionalRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleConditionalRule(id));
    }

    // ── Required Document Rules ───────────────────────────────────────────────

    @Operation(summary = "List all required-doc rules (across all templates)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved")
    @GetMapping("/required-docs")
    public ResponseEntity<List<RequiredDocRuleDto>> getAllRequiredDocRules() {
        return ResponseEntity.ok(adminRuleService.getAllRequiredDocRules());
    }

    @Operation(summary = "Toggle required-doc rule active/inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule toggled"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PatchMapping("/required-docs/{id}/toggle")
    public ResponseEntity<RequiredDocRuleDto> toggleRequiredDocRule(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(adminRuleService.toggleRequiredDocRule(id));
    }
}
