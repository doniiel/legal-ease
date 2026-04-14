package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lawyer-documents")
@Tag(name = "Lawyer - Document View")
@RequiredArgsConstructor
public class LawyerDocumentController {

    private final LawyerDocumentService lawyerDocumentService;

    @Operation(summary = "List all documents from my templates (LAWYER only)")
    @GetMapping
    public ResponseEntity<Page<DocumentPreviewDto>> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lawyerDocumentService.getDocumentsByMyTemplates(pageable));
    }

    @Operation(summary = "List documents for a specific template (LAWYER only)")
    @GetMapping("/by-template/{templateId}")
    public ResponseEntity<Page<DocumentPreviewDto>> getByTemplate(
            @Parameter(description = "Template ID", required = true) @NotNull @PathVariable Long templateId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lawyerDocumentService.getDocumentsByTemplate(templateId, pageable));
    }

    @Operation(summary = "Get document detail (LAWYER only)")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getById(
            @Parameter(description = "Document ID", required = true) @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(lawyerDocumentService.getDocumentById(id));
    }
}
