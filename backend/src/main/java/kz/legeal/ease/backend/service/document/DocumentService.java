package kz.legeal.ease.backend.service.document;

import kz.legeal.ease.backend.dto.CompleteDocumentResponseDto;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.request.document.UpdateDocumentRequest;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

    DocumentDto createDocument(CreateDocumentRequest request);

    Page<DocumentPreviewDto> getMyDocuments(Pageable pageable);

    DocumentDto getMyDocumentById(Long docId);

    DocumentDto update(Long docId, UpdateDocumentRequest req);

    CompleteDocumentResponseDto complete(Long docId);

    RuleEngineResult getSuggestions(Long docId);

    void deleteDocument(Long docId);
}
