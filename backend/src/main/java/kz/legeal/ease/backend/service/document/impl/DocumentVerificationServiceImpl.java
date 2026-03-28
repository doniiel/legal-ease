package kz.legeal.ease.backend.service.document.impl;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.dto.document.DocumentVerificationResponse;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.service.document.DocumentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DocumentVerificationServiceImpl implements DocumentVerificationService {

    private final DocumentRepository documentRepository;

    /**
     * Verification logic:
     * <ol>
     *   <li>Document not found → {@code INVALID}, no details.</li>
     *   <li>Document soft-deleted → {@code INVALID}.</li>
     *   <li>{@code contentHash} is null → {@code INVALID} (not properly completed).</li>
     *   <li>{@code contentHash} present → {@code VALID}.</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentVerificationResponse verify(Long documentId) {
        return documentRepository.findById(documentId)
                .map(doc -> buildResponse(documentId, doc))
                .orElseGet(() -> invalidResponse(documentId));
    }

    private DocumentVerificationResponse buildResponse(Long documentId, Document doc) {
        if (doc.isDeleted()) {
            return invalidResponse(documentId);
        }

        final boolean hashValid = doc.getContentHash() != null;

        return DocumentVerificationResponse.builder()
                .status(hashValid ? "VALID" : "INVALID")
                .documentId(formatDocRef(documentId))
                .createdAt(doc.getCreatedDate() != null
                        ? doc.getCreatedDate().toLocalDate()
                        : LocalDate.now())
                .createdBy(resolveCreatedBy(doc))
                .hashValid(hashValid)
                .build();
    }

    private static DocumentVerificationResponse invalidResponse(Long documentId) {
        return DocumentVerificationResponse.builder()
                .status("INVALID")
                .documentId(formatDocRef(documentId))
                .hashValid(false)
                .build();
    }

    private static String resolveCreatedBy(Document doc) {
        if (doc.getUser() != null
                && doc.getUser().getFio() != null
                && !doc.getUser().getFio().isBlank()) {
            return doc.getUser().getFio();
        }
        return doc.getCreatedBy();
    }

    private static String formatDocRef(Long id) {
        return "DOC-" + String.format("%06d", id);
    }
}
