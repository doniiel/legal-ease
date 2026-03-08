package kz.legeal.ease.backend.controller.lawyer;

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
public class ConditionalRuleController {

    private final ConditionalRuleService conditionalRuleService;

    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ConditionalRuleDto>> getByTemplate(
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(conditionalRuleService.getAllByTemplate(templateId));
    }

    @PostMapping
    public ResponseEntity<ConditionalRuleDto> create(
            @RequestBody ConditionalRuleRequest request
    ) {
        return ResponseEntity.ok(conditionalRuleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConditionalRuleDto> update(
            @PathVariable Long id,
            @RequestBody ConditionalRuleRequest request
    ) {
        return ResponseEntity.ok(conditionalRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        conditionalRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
