package kz.legeal.ease.backend.service.document.impl;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.DocumentFieldValue;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.enums.DocumentStatus;
import kz.legeal.ease.backend.exception.BusinessException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.DocumentMapper;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.request.document.UpdateDocumentRequest;
import kz.legeal.ease.backend.service.document.DocumentService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TemplateRepository templateRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest request) {
        final var currentUser = requireCurrentUser();

        final var template = templateRepository.findByIdAndActive(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException("Template", request.getTemplateId()));

        if (!template.isPublished()) {
            throw new BusinessException("Cannot create document from unpublished template");
        }

        final var fieldValues = buildFieldValues(request.getFieldValues(), null, template);
        validateUnknownKeys(request.getFieldValues(), template);

        final var document = Document.builder()
                .user(currentUser)
                .template(template)
                .title(request.getTitle())
                .status(DocumentStatus.DRAFT)
                .deleted(false)
                .fieldValues(fieldValues)
                .build();

        fieldValues.forEach(fv -> fv.setDocument(document));

        final var saved = documentRepository.save(document);
        log.info("User id={} created document id={} from template id={}", currentUser.getId(), saved.getId(), template.getId());
        return documentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentPreviewDto> getMyDocuments(Pageable pageable) {
        final var currentUser = requireCurrentUser();
        return documentRepository
                .findAllByUserIdAndNotDeleted(currentUser.getId(), pageable)
                .map(documentMapper::toPreviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getMyDocumentById(Long docId) {
        final var currentUser = requireCurrentUser();
        final var document = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("Document", docId));
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional
    public DocumentDto update(Long docId, UpdateDocumentRequest req) {
        final var currentUser = requireCurrentUser();
        final var doc = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("Document", docId));

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BusinessException("Only DRAFT documents can be edited", "DOC_NOT_EDITABLE");
        }

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            doc.setTitle(req.getTitle());
        }

        if (req.getFieldValues() != null) {
            validateUnknownKeys(req.getFieldValues(), doc.getTemplate());
            doc.getFieldValues().clear();
            final var newValues = buildFieldValues(req.getFieldValues(), doc, doc.getTemplate());
            newValues.forEach(fv -> fv.setDocument(doc));
            doc.getFieldValues().addAll(newValues);
        }

        log.info("User id={} updated document id={}", currentUser.getId(), docId);
        return documentMapper.toDto(doc);
    }

    @Override
    @Transactional
    public DocumentDto complete(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BusinessException("Document is already completed", "DOC_ALREADY_COMPLETED");
        }

        final var missingFields = findMissingRequiredFields(doc);
        if (!missingFields.isEmpty()) {
            throw new BusinessException(
                    "Cannot complete document. Missing required fields: " + String.join(", ", missingFields),
                    "DOC_VALIDATION_FAILED"
            );
        }

        doc.setStatus(DocumentStatus.COMPLETED);
        log.info("User id={} completed document id={}", currentUser.getId(), docId);
        return documentMapper.toDto(doc);
    }

    @Override
    @Transactional
    public void deleteDocument(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);
        doc.setDeleted(true);
        log.info("User id={} deleted document id={}", currentUser.getId(), docId);
    }

    private Document findOwnedOrThrow(Long userId, Long docId) {
        return documentRepository.findByIdAndUserIdAndNotDeleted(docId, userId)
                .orElseThrow(() -> new NotFoundException("Document", docId));
    }

    private List<DocumentFieldValue> buildFieldValues(
            Map<String, String> values,
            Document doc,
            Template template) {

        if (values == null || values.isEmpty()) return new ArrayList<>();

        return values.entrySet().stream()
                .map(e -> DocumentFieldValue.builder()
                        .document(doc)
                        .fieldKey(e.getKey())
                        .fieldValue(e.getValue() != null ? e.getValue().trim() : "")
                        .build())
                .collect(Collectors.toCollection(java.util.ArrayList::new));
    }

    private void validateUnknownKeys(Map<String, String> values, Template template) {
        if (values == null || values.isEmpty()) return;

        final var validKeys = template.getTemplateFields().stream()
                .map(kz.legeal.ease.backend.domain.TemplateField::getFieldKey)
                .collect(Collectors.toSet());

        final var unknownKeys = values.keySet().stream()
                .filter(k -> !validKeys.contains(k))
                .toList();

        if (!unknownKeys.isEmpty()) {
            throw new BusinessException(
                    "Unknown field keys: " + String.join(", ", unknownKeys),
                    "DOC_UNKNOWN_FIELDS"
            );
        }
    }

    private List<String> findMissingRequiredFields(Document doc) {
        final Map<String, String> savedValues = doc.getFieldValues().stream()
                .collect(Collectors.toMap(
                        DocumentFieldValue::getFieldKey,
                        DocumentFieldValue::getFieldValue
                ));

        return doc.getTemplate().getTemplateFields().stream()
                .filter(kz.legeal.ease.backend.domain.TemplateField::isRequired)
                .filter(tf -> {
                    final var val = savedValues.get(tf.getFieldKey());
                    return val == null || val.isBlank();
                })
                .map(kz.legeal.ease.backend.domain.TemplateField::getLabel)
                .toList();
    }

    private User requireCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

}
