package kz.legeal.ease.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.CompleteDocumentResponseDto;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.dto.document.DocumentShareResponse;
import kz.legeal.ease.backend.dto.document.DocumentVersionDto;
import kz.legeal.ease.backend.dto.document.PresignedUrlResponse;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.request.document.UpdateDocumentRequest;
import kz.legeal.ease.backend.service.document.DocumentService;
import kz.legeal.ease.backend.service.document.DocumentShareService;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentShareService documentShareService;

    @Operation(summary = "Create a new document")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PostMapping
    public ResponseEntity<DocumentDto> create(@Valid @RequestBody CreateDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createDocument(request));
    }

    @Operation(summary = "List my documents")
    @ApiResponse(responseCode = "200", description = "Documents retrieved")
    @GetMapping
    public ResponseEntity<Page<DocumentPreviewDto>> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(documentService.getMyDocuments(pageable));
    }

    @Operation(summary = "List my documents including archived")
    @ApiResponse(responseCode = "200", description = "Documents retrieved")
    @GetMapping("/all")
    public ResponseEntity<Page<DocumentPreviewDto>> getAllIncludingArchived(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(documentService.getMyDocumentsIncludingArchived(pageable));
    }

    @Operation(summary = "Get document by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getById(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getMyDocumentById(id));
    }

    @Operation(summary = "Update a DRAFT document")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> update(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    @Operation(summary = "Get rule engine suggestions")
    @GetMapping("/{id}/suggestions")
    public ResponseEntity<RuleEngineResult> getSuggestions(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getSuggestions(id));
    }

    @Operation(summary = "Validate a document (DRAFT → VALIDATED)")
    @PostMapping("/{id}/validate")
    public ResponseEntity<RuleEngineResult> validate(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.validate(id));
    }

    @Operation(summary = "AI analysis (no status change)")
    @PostMapping("/{id}/analyze")
    public ResponseEntity<RuleEngineResult> analyzeWithAi(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.analyzeWithAi(id));
    }

    @Operation(summary = "Archive a COMPLETED document")
    @PostMapping("/{id}/archive")
    public ResponseEntity<DocumentDto> archive(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.archive(id));
    }

    @Operation(summary = "Restore an ARCHIVED document back to DRAFT")
    @PostMapping("/{id}/restore")
    public ResponseEntity<DocumentDto> restore(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.restore(id));
    }

    @Operation(summary = "Complete a document (DRAFT → COMPLETED)")
    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteDocumentResponseDto> complete(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.complete(id));
    }

    @Operation(summary = "Delete a DRAFT document")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Download PDF")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        final byte[] pdfBytes = documentService.downloadDocument(id);
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("document-" + id + ".pdf").build());
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "Get presigned download URL")
    @GetMapping("/{id}/url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getPresignedUrl(id));
    }

    @Operation(summary = "Create share link")
    @PostMapping("/{id}/share")
    public ResponseEntity<DocumentShareResponse> share(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentShareService.createShareLink(id));
    }

    @Operation(summary = "List document versions")
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<DocumentVersionDto>> getVersions(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getVersions(id));
    }

    @Operation(summary = "Download specific version")
    @GetMapping("/{id}/versions/{version}/download")
    public ResponseEntity<byte[]> downloadVersion(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id,
            @Parameter(description = "Version number (1-based)", required = true) @PathVariable int version
    ) {
        final byte[] pdfBytes = documentService.downloadVersion(id, version);
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("document-" + id + "-v" + version + ".pdf").build());
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
