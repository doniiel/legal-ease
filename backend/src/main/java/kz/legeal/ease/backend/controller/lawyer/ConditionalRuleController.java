package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.ConditionalRuleDto;
import kz.legeal.ease.backend.request.ConditionalRuleRequest;
import kz.legeal.ease.backend.service.ConditionalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lawyer/conditional-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Conditional Rules",
        description = "Manage conditional rules for templates"
)
public class ConditionalRuleController {

    private final ConditionalRuleService conditionalRuleService;

    @Operation(summary = "Get conditional rules by template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ConditionalRuleDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(conditionalRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create conditional rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<ConditionalRuleDto> create(
            @Valid @RequestBody ConditionalRuleRequest request
    ) {
        return ResponseEntity.ok(conditionalRuleService.create(request));
    }

    @Operation(summary = "Update conditional rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConditionalRuleDto> update(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ConditionalRuleRequest request
    ) {
        return ResponseEntity.ok(conditionalRuleService.update(id, request));
    }

    @Operation(summary = "Delete conditional rule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Long id
    ) {
        conditionalRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
