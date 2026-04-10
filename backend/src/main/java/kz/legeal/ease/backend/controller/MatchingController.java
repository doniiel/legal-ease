package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.request.MatchingRequest;
import kz.legeal.ease.backend.service.MatchingService;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Tag(name = "Template Matching")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "Find matching templates")
    @PostMapping
    public ResponseEntity<RuleEngineResult> match(@Valid @RequestBody MatchingRequest request) {
        return ResponseEntity.ok(matchingService.match(request));
    }
}
