package kz.legeal.ease.backend.controller.lawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.service.lawyer.LawyerDocumentService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lawyer/documents")
@PreAuthorize("hasRole('LAWYER')")
@Tag(
        name = "Lawyer - Document View",
        description = "Read-only access to documents created from the lawyer's templates"
)
@RequiredArgsConstructor
public class LawyerDocumentController {

    private final LawyerDocumentService lawyerDocumentService;

    @Operation(
            summary = "List all documents from my templates",
            description = "Returns all non-deleted documents created from any template owned by the current lawyer."
    )
    @ApiResponse(responseCode = "200", description = "Documents returned")
    @GetMapping
    public ResponseEntity<Page<DocumentPreviewDto>> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lawyerDocumentService.getDocumentsByMyTemplates(pageable));
    }

    @Operation(
            summary = "List documents for a specific template",
            description = "Returns documents scoped to a single template. The template must belong to the current lawyer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents returned"),
            @ApiResponse(responseCode = "403", description = "Template does not belong to the current lawyer")
    })
    @GetMapping("/by-template/{templateId}")
    public ResponseEntity<Page<DocumentPreviewDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long templateId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lawyerDocumentService.getDocumentsByTemplate(templateId, pageable));
    }

    @Operation(
            summary = "Get document detail",
            description = "Returns full document detail. The document must have been created from one of the lawyer's templates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getById(
            @Parameter(description = "Document ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(lawyerDocumentService.getDocumentById(id));
    }
}
