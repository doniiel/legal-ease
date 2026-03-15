package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.RequiredDocRuleDto;
import kz.legeal.ease.backend.request.RequiredDocRuleRequest;
import kz.legeal.ease.backend.service.RequiredDocRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Lawyer-facing CRUD for Required Document Rules.
 *
 * <p>A Required Document Rule tells users which supporting documents they must
 * (or should) prepare alongside the main document created from a given template.
 * Rules can be unconditional or conditional (based on field values).
 */
@RestController
@RequestMapping("/api/lawyer/required-doc-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Required Document Rules",
        description = "Define which supporting documents a user must provide for a given template"
)
public class LawyerRequiredDocRuleController {

    private final RequiredDocRuleService requiredDocRuleService;

    @Operation(summary = "Get required-doc rules by template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<RequiredDocRuleDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(requiredDocRuleService.getAllByTemplate(templateId));
    }

    @Operation(
            summary = "Create a required-doc rule",
            description = "Define a new document requirement for a template. " +
                    "Leave conditionFieldKey null to make it unconditional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PostMapping
    public ResponseEntity<RequiredDocRuleDto> create(
            @Valid @RequestBody RequiredDocRuleRequest request
    ) {
        return ResponseEntity.ok(requiredDocRuleService.create(request));
    }

    @Operation(summary = "Update a required-doc rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RequiredDocRuleDto> update(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RequiredDocRuleRequest request
    ) {
        return ResponseEntity.ok(requiredDocRuleService.update(id, request));
    }

    @Operation(summary = "Soft-delete (deactivate) a required-doc rule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Long id
    ) {
        requiredDocRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
