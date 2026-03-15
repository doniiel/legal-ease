package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.LegalClauseDto;
import kz.legeal.ease.backend.request.LegalClauseRequest;
import kz.legeal.ease.backend.service.lawyer.LegalClauseService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lawyer/clauses")
@PreAuthorize("hasRole('LAWYER')")
@Tag(name = "Lawyer - Clause Knowledge Base", description = "Manage the lawyer's library of reusable legal clauses")
@RequiredArgsConstructor
public class LawyerClauseController {

    private final LegalClauseService clauseService;

    @Operation(summary = "Create a new clause")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clause created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<LegalClauseDto> create(
            @Valid @RequestBody LegalClauseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clauseService.create(request));
    }

    @Operation(
            summary = "Search clauses",
            description = "Returns active clauses owned by the current lawyer, optionally filtered by category or keyword (matches title and tags)."
    )
    @ApiResponse(responseCode = "200", description = "Clauses returned")
    @GetMapping
    public ResponseEntity<Page<LegalClauseDto>> search(
            @Parameter(description = "Optional category ID filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Optional keyword (matches title and tags)") @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(clauseService.search(categoryId, keyword, pageable));
    }

    @Operation(summary = "Get clause by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clause found"),
            @ApiResponse(responseCode = "404", description = "Clause not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LegalClauseDto> getById(
            @Parameter(description = "Clause ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(clauseService.getById(id));
    }

    @Operation(summary = "Update a clause")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clause updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Clause not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LegalClauseDto> update(
            @Parameter(description = "Clause ID", required = true) @NotNull @PathVariable Long id,
            @Valid @RequestBody LegalClauseRequest request
    ) {
        return ResponseEntity.ok(clauseService.update(id, request));
    }

    @Operation(summary = "Delete (soft-deactivate) a clause")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Clause deleted"),
            @ApiResponse(responseCode = "404", description = "Clause not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Clause ID", required = true) @NotNull @PathVariable Long id
    ) {
        clauseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
