package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.ValidationRuleDto;
import kz.legeal.ease.backend.request.ValidationRuleRequest;
import kz.legeal.ease.backend.service.ValidationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lawyer/validation-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Validation Rules",
        description = "Manage validation rules for template fields"
)
public class ValidationRuleController {

    private final ValidationRuleService validationRuleService;

    @Operation(summary = "Get validation rules by template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ValidationRuleDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(validationRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create validation rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<ValidationRuleDto> create(
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        return ResponseEntity.ok(validationRuleService.create(request));
    }

    @Operation(summary = "Update validation rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ValidationRuleDto> update(
            @Parameter(description = "Validation rule ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        return ResponseEntity.ok(validationRuleService.update(id, request));
    }

    @Operation(summary = "Delete validation rule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Validation rule ID", required = true)
            @PathVariable Long id
    ) {
        validationRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
