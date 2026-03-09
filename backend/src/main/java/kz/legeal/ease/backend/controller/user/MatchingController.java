package kz.legeal.ease.backend.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.request.MatchingRequest;
import kz.legeal.ease.backend.service.MatchingService;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/matching")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(
        name = "User - Template Matching",
        description = "Match user input with suitable legal templates"
)
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(
            summary = "Find matching templates",
            description = "User provides text or parameters and receives a list of matching document templates"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    public ResponseEntity<RuleEngineResult> match(
            @Valid @RequestBody MatchingRequest request
    ) {
        return ResponseEntity.ok(matchingService.match(request));
    }
}
