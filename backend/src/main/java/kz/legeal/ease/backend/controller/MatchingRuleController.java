package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.MatchingRuleDto;
import kz.legeal.ease.backend.request.MatchingRuleRequest;
import kz.legeal.ease.backend.service.MatchingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching-rules")
@RequiredArgsConstructor
@Tag(name = "Matching Rules (Lawyer)")
public class MatchingRuleController {

    private final MatchingRuleService matchingRuleService;

    @Operation(summary = "Get all matching rules (LAWYER only)")
    @GetMapping
    public ResponseEntity<List<MatchingRuleDto>> getAll() {
        return ResponseEntity.ok(matchingRuleService.getAll());
    }

    @Operation(summary = "Get matching rules by template (LAWYER only)")
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<MatchingRuleDto>> getByTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(matchingRuleService.getAllByTemplate(templateId));
    }

    @Operation(summary = "Create matching rule (LAWYER only)")
    @PostMapping
    public ResponseEntity<MatchingRuleDto> create(@Valid @RequestBody MatchingRuleRequest request) {
        return ResponseEntity.ok(matchingRuleService.create(request));
    }

    @Operation(summary = "Update matching rule (LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<MatchingRuleDto> update(
            @Parameter(description = "Rule ID", required = true) @PathVariable Long id,
            @Valid @RequestBody MatchingRuleRequest request
    ) {
        return ResponseEntity.ok(matchingRuleService.update(id, request));
    }

    @Operation(summary = "Delete matching rule (LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Rule ID", required = true) @PathVariable Long id) {
        matchingRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
