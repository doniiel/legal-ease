package kz.legeal.ease.backend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.ai.ClauseExplainResponse;
import kz.legeal.ease.backend.request.ai.ClauseExplainRequest;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "AI - Legal Assistance",
        description = "AI-powered legal clause explanation and analysis"
)
public class AiController {

    private final RuleAiService ruleAiService;

    @Operation(
            summary = "Explain a legal clause",
            description = "Submits a legal clause text to the AI and returns a plain-language explanation. " +
                    "Useful for users who do not understand complex legal terminology."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Explanation generated successfully"),
            @ApiResponse(responseCode = "400", description = "Clause text is blank or too long"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/explain-clause")
    public ResponseEntity<ClauseExplainResponse> explainClause(
            @Valid @RequestBody ClauseExplainRequest request
    ) {
        final String explanation = ruleAiService.explainClause(request.getText());
        return ResponseEntity.ok(new ClauseExplainResponse(explanation));
    }
}
