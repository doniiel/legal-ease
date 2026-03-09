package kz.legeal.ease.backend.controller.user;

import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.service.document.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @GetMapping
    public ResponseEntity<Page<DocumentPreviewDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(documentService.getMyDocuments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getMyDocumentById(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<DocumentDto> completeDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
