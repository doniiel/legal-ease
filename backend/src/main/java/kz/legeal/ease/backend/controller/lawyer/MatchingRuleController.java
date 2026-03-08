package kz.legeal.ease.backend.controller.lawyer;

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
public class MatchingRuleController {

    private final MatchingRuleService matchingRuleService;

    @GetMapping
    public ResponseEntity<List<MatchingRuleDto>> getAll() {
        return ResponseEntity.ok(matchingRuleService.getAll());
    }

    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<MatchingRuleDto>> getByTemplate(
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(matchingRuleService.getAllByTemplate(templateId));
    }

    @PostMapping
    public ResponseEntity<MatchingRuleDto> create(
            @Valid @RequestBody MatchingRuleRequest request
    ) {
        return ResponseEntity.ok(matchingRuleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchingRuleDto> update(
            @PathVariable Long id,
            @Valid @RequestBody MatchingRuleRequest request
    ) {
        return ResponseEntity.ok(matchingRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchingRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
