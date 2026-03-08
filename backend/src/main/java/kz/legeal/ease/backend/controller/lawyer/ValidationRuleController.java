package kz.legeal.ease.backend.controller.lawyer;

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
public class ValidationRuleController {

    private final ValidationRuleService validationRuleService;

    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<ValidationRuleDto>> getByTemplate(
            @PathVariable Long templateId
    ) {
        return ResponseEntity.ok(validationRuleService.getAllByTemplate(templateId));
    }

    @PostMapping
    public ResponseEntity<ValidationRuleDto> create(
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        return ResponseEntity.ok(validationRuleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ValidationRuleDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        return ResponseEntity.ok(validationRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        validationRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
