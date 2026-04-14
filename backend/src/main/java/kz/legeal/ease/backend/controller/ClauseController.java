package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clauses")
@Tag(name = "Clause Knowledge Base (Lawyer)")
@RequiredArgsConstructor
public class ClauseController {

    private final LegalClauseService clauseService;

    @Operation(summary = "Create a new clause (LAWYER only)")
    @PostMapping
    public ResponseEntity<LegalClauseDto> create(@Valid @RequestBody LegalClauseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clauseService.create(request));
    }

    @Operation(summary = "Search clauses (LAWYER only)")
    @GetMapping
    public ResponseEntity<Page<LegalClauseDto>> search(
            @Parameter(description = "Optional category ID filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Optional keyword") @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(clauseService.search(categoryId, keyword, pageable));
    }

    @Operation(summary = "Get clause by ID (LAWYER only)")
    @GetMapping("/{id}")
    public ResponseEntity<LegalClauseDto> getById(
            @Parameter(description = "Clause ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(clauseService.getById(id));
    }

    @Operation(summary = "Update a clause (LAWYER only)")
    @PutMapping("/{id}")
    public ResponseEntity<LegalClauseDto> update(
            @Parameter(description = "Clause ID", required = true) @NotNull @PathVariable Long id,
            @Valid @RequestBody LegalClauseRequest request
    ) {
        return ResponseEntity.ok(clauseService.update(id, request));
    }

    @Operation(summary = "Delete a clause (LAWYER only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Clause ID", required = true) @NotNull @PathVariable Long id
    ) {
        clauseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
