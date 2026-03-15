package kz.legeal.ease.backend.controller.user;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(
        name = "User - Document Management",
        description = "Create, update, complete, download, and share user legal documents"
)
public class UserDocumentController {

    private final DocumentService      documentService;
    private final DocumentShareService documentShareService;

    @Operation(summary = "Create a new document", description = "Creates a DRAFT document from a published template with initial field values")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PostMapping
    public ResponseEntity<DocumentDto> create(
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @Operation(summary = "List my documents", description = "Returns a paginated list of the authenticated user's active (non-archived) documents")
    @ApiResponse(responseCode = "200", description = "Documents retrieved")
    @GetMapping
    public ResponseEntity<Page<DocumentPreviewDto>> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(documentService.getMyDocuments(pageable));
    }

    @Operation(summary = "List my documents including archived", description = "Returns all documents including ARCHIVED ones")
    @ApiResponse(responseCode = "200", description = "Documents retrieved")
    @GetMapping("/all")
    public ResponseEntity<Page<DocumentPreviewDto>> getAllIncludingArchived(
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable
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

    @Operation(summary = "Update a DRAFT document", description = "Updates title and/or field values of a document in DRAFT status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or document not in DRAFT status"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> update(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    @Operation(summary = "Get rule engine suggestions", description = "Runs the rule engine against the document and returns validation errors, risk warnings, and required documents")
    @ApiResponse(responseCode = "200", description = "Suggestions returned")
    @GetMapping("/{id}/suggestions")
    public ResponseEntity<RuleEngineResult> getSuggestions(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getSuggestions(id));
    }

    @Operation(
            summary = "Validate a document (DRAFT → VALIDATED)",
            description = "Runs the full rule engine validation chain. If all rules pass, the document status moves to VALIDATED. " +
                    "If any rule fails, status stays DRAFT and errors are returned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result returned (check result for errors)"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}/validate")
    public ResponseEntity<RuleEngineResult> validate(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.validate(id));
    }

    @Operation(
            summary = "AI analysis (no status change)",
            description = "Runs AI risk analysis and field suggestions without changing the document status. Rate-limited to 10 requests/min."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI analysis result returned"),
            @ApiResponse(responseCode = "429", description = "AI rate limit exceeded"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}/analyze")
    public ResponseEntity<RuleEngineResult> analyzeWithAi(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.analyzeWithAi(id));
    }

    @Operation(
            summary = "Archive a COMPLETED document",
            description = "Moves a COMPLETED document to ARCHIVED status and revokes all active share links."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document archived"),
            @ApiResponse(responseCode = "400", description = "Document is not in COMPLETED status"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}/archive")
    public ResponseEntity<DocumentDto> archive(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.archive(id));
    }

    @Operation(
            summary = "Restore an ARCHIVED document back to DRAFT",
            description = "Moves an ARCHIVED document back to DRAFT status for re-editing. Version history is preserved."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document restored to DRAFT"),
            @ApiResponse(responseCode = "400", description = "Document is not in ARCHIVED status"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<DocumentDto> restore(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.restore(id));
    }

    @Operation(summary = "Complete a document (DRAFT → COMPLETED)", description = "Runs full rule validation, generates a versioned PDF and stores it in S3/MinIO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document completed and PDF generated"),
            @ApiResponse(responseCode = "400", description = "Validation errors present"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteDocumentResponseDto> complete(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.complete(id));
    }

    @Operation(summary = "Delete a DRAFT document")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document deleted"),
            @ApiResponse(responseCode = "400", description = "Document is not in DRAFT status"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Download PDF", description = "Streams the completed document's PDF directly from S3/MinIO as an attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF returned"),
            @ApiResponse(responseCode = "404", description = "Document not found or not completed yet")
    })
    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        final byte[] pdfBytes = documentService.downloadDocument(id);

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("document-" + id + ".pdf")
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "Get presigned download URL", description = "Returns a time-limited presigned URL for direct PDF access without an Authorization header")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL generated"),
            @ApiResponse(responseCode = "404", description = "Document not found or not completed")
    })
    @GetMapping("/{id}/url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getPresignedUrl(id));
    }

    @Operation(summary = "Create share link", description = "Creates a time-limited public share link for a completed document. Anyone with the token can download the PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share link created"),
            @ApiResponse(responseCode = "400", description = "Document is not completed"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}/share")
    public ResponseEntity<DocumentShareResponse> share(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentShareService.createShareLink(id));
    }

    @Operation(summary = "List document versions", description = "Returns all immutable PDF versions created each time the document was completed")
    @ApiResponse(responseCode = "200", description = "Versions retrieved")
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<DocumentVersionDto>> getVersions(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getVersions(id));
    }

    @Operation(summary = "Download specific version", description = "Downloads the PDF for a specific immutable document version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Version PDF returned"),
            @ApiResponse(responseCode = "404", description = "Document or version not found")
    })
    @GetMapping(value = "/{id}/versions/{version}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadVersion(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id,
            @Parameter(description = "Version number (1-based)", required = true) @PathVariable int version
    ) {
        final byte[] pdfBytes = documentService.downloadVersion(id, version);

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("document-" + id + "-v" + version + ".pdf")
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}