package kz.legeal.ease.backend.controller.user;

import jakarta.validation.Valid;
import kz.legeal.ease.backend.dto.CompleteDocumentResponseDto;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentFieldValueDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.service.document.DocumentService;
import kz.legeal.ease.backend.service.rule.context.RuleContext;
import kz.legeal.ease.backend.service.rule.engine.RuleEngine;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    private final DocumentService documentService;
    private final RuleEngine ruleEngine;

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

    // Получить AI подсказки для незаполненных полей
    @GetMapping("/{id}/suggestions")
    public ResponseEntity<RuleEngineResult> getSuggestions(@PathVariable Long id) {
        final var doc = documentService.getMyDocumentById(id);
        final var context = RuleContext.builder()
                .templateId(doc.getTemplateId())
                .fieldValues(transformFieldValues(doc))
                .build();
        return ResponseEntity.ok(ruleEngine.suggestFields(context));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteDocumentResponseDto> complete(@PathVariable Long id) {
        final var doc = documentService.getMyDocumentById(id);
        final var context = RuleContext.builder()
                .templateId(doc.getTemplateId())
                .fieldValues(transformFieldValues(doc))
                .documentText(buildDocumentText(doc))
                .build();

        final var ruleResult = ruleEngine.complete(context);

        if (!ruleResult.isValid()) {
            return ResponseEntity.ok(CompleteDocumentResponseDto.failed(ruleResult));
        }

        final var completed = documentService.complete(id);
        return ResponseEntity.ok(CompleteDocumentResponseDto.success(completed, ruleResult));
    }

    private Map<String, String> transformFieldValues(DocumentDto doc) {
        return doc.getFieldValues()
                .stream()
                .collect(Collectors.toMap(
                        DocumentFieldValueDto::getFieldKey,
                        DocumentFieldValueDto::getFieldValue
                ));
    }

    private String buildDocumentText(DocumentDto doc) {
        final var sb = new StringBuilder();
        sb.append("Документ: ").append(doc.getTitle()).append("\n");
        sb.append("Шаблон: ").append(doc.getTemplateTitle()).append("\n\n");
        doc.getFieldValues().forEach(fv ->
                sb.append(fv.getFieldKey()).append(": ").append(fv.getFieldValue()).append("\n")
        );
        return sb.toString();
    }
}
