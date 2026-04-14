package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.ai.ClauseExplainResponse;
import kz.legeal.ease.backend.request.ai.ClauseExplainRequest;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI - Legal Assistance")
public class AiController {

    private final RuleAiService ruleAiService;

    @Operation(summary = "Explain a legal clause (any authenticated user)")
    @PostMapping("/explain-clause")
    public ResponseEntity<ClauseExplainResponse> explainClause(@Valid @RequestBody ClauseExplainRequest request) {
        final String explanation = ruleAiService.explainClause(request.getText());
        return ResponseEntity.ok(new ClauseExplainResponse(explanation));
    }
}
