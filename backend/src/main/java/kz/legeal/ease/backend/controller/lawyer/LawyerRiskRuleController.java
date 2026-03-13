package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.RiskRuleDto;
import kz.legeal.ease.backend.request.RiskRuleRequest;
import kz.legeal.ease.backend.service.RiskRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/lawyer/risk-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Risk Rules",
        description = "Manage risk analysis rules for templates"
)
public class LawyerRiskRuleController {

    private final RiskRuleService riskRuleService;

    @Operation(summary = "Get risk rules by template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<RiskRuleDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(riskRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create risk rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<RiskRuleDto> create(
            @Valid @RequestBody RiskRuleRequest request
    ) {
        return ResponseEntity.ok(riskRuleService.create(request));
    }

    @Operation(summary = "Update risk rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RiskRuleDto> update(
            @Parameter(description = "Risk rule ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RiskRuleRequest request
    ) {
        return ResponseEntity.ok(riskRuleService.update(id, request));
    }

    @Operation(summary = "Delete risk rule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Risk rule ID", required = true)
            @PathVariable Long id
    ) {
        riskRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
