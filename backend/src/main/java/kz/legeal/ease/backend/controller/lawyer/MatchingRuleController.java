package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.MatchingRuleDto;
import kz.legeal.ease.backend.request.MatchingRuleRequest;
import kz.legeal.ease.backend.service.MatchingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lawyer/matching-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Matching Rules",
        description = "Manage AI matching rules for templates"
)
public class MatchingRuleController {

    private final MatchingRuleService matchingRuleService;

    @Operation(summary = "Get all matching rules")
    @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    @GetMapping
    public ResponseEntity<List<MatchingRuleDto>> getAll() {
        return ResponseEntity.ok(matchingRuleService.getAll());
    }

    @Operation(summary = "Get matching rules by template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<MatchingRuleDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(matchingRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create matching rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<MatchingRuleDto> create(
            @Valid @RequestBody MatchingRuleRequest request
    ) {
        return ResponseEntity.ok(matchingRuleService.create(request));
    }

    @Operation(summary = "Update matching rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MatchingRuleDto> update(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody MatchingRuleRequest request
    ) {
        return ResponseEntity.ok(matchingRuleService.update(id, request));
    }

    @Operation(summary = "Delete matching rule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable Long id
    ) {
        matchingRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
