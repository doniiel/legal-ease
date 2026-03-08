package kz.legeal.ease.backend.controller.lawyer;

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
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<RiskRuleDto>> getByTemplate(
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(riskRuleService.getAllByTemplate(templateId));
    }

    @PostMapping
    public ResponseEntity<RiskRuleDto> create(
            @Valid @RequestBody RiskRuleRequest request
    ) {
        return ResponseEntity.ok(riskRuleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RiskRuleDto> update(
            @PathVariable Long id,
            @Valid @RequestBody RiskRuleRequest request
    ) {
        return ResponseEntity.ok(riskRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        riskRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
