package kz.legeal.ease.backend.service.document.impl;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.DocumentFieldValue;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.CompleteDocumentResponseDto;
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
import kz.legeal.ease.backend.service.rule.context.RuleContext;
import kz.legeal.ease.backend.service.rule.engine.RuleEngine;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TemplateRepository templateRepository;
    private final DocumentMapper documentMapper;
    private final RuleEngine ruleEngine;

    @Override
    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest request) {
        final var currentUser = requireCurrentUser();

        final var template = templateRepository.findByIdAndActive(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException("Template", request.getTemplateId()));

        if (!template.isPublished()) {
            throw new BusinessException("Cannot create document from unpublished template");
        }

        validateUnknownKeys(request.getFieldValues(), template);

        final var document = Document.builder()
                .user(currentUser)
                .template(template)
                .title(request.getTitle())
                .status(DocumentStatus.DRAFT)
                .deleted(false)
                .fieldValues(new ArrayList<>())
                .build();

        final var fieldValues = buildFieldValues(request.getFieldValues(), document);
        document.getFieldValues().addAll(fieldValues);

        final var saved = documentRepository.save(document);
        log.info("User id={} created document id={} from template id={}",
                currentUser.getId(), saved.getId(), template.getId());
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
        // ✅ Исправлено: было findById без проверки владельца — любой USER мог читать чужой документ
        final var document = findOwnedOrThrow(currentUser.getId(), docId);
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional
    public DocumentDto update(Long docId, UpdateDocumentRequest req) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BusinessException("Only DRAFT documents can be edited", "DOC_NOT_EDITABLE");
        }

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            doc.setTitle(req.getTitle());
        }

        if (req.getFieldValues() != null) {
            validateUnknownKeys(req.getFieldValues(), doc.getTemplate());
            doc.getFieldValues().clear();
            final var newValues = buildFieldValues(req.getFieldValues(), doc);
            doc.getFieldValues().addAll(newValues);
        }

        log.info("User id={} updated document id={}", currentUser.getId(), docId);
        return documentMapper.toDto(doc);
    }

    @Override
    @Transactional
    public CompleteDocumentResponseDto complete(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BusinessException("Document is already completed", "DOC_ALREADY_COMPLETED");
        }

        validateRequiredTemplateFields(doc);

        final var fieldValues = extractFieldValues(doc);
        final var context = RuleContext.builder()
                .templateId(doc.getTemplate().getId())
                .fieldValues(fieldValues)
                .documentText(buildDocumentText(doc))
                .build();

        final var ruleResult = ruleEngine.complete(context);

        if (!ruleResult.isValid()) {
            log.warn("Document id={} failed rule validation: {} errors",
                    docId, ruleResult.getValidationErrors().size());
            return CompleteDocumentResponseDto.failed(ruleResult);
        }

        doc.setStatus(DocumentStatus.COMPLETED);
        log.info("User id={} completed document id={}", currentUser.getId(), docId);
        return CompleteDocumentResponseDto.success(documentMapper.toDto(doc), ruleResult);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleEngineResult getSuggestions(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        final var context = RuleContext.builder()
                .templateId(doc.getTemplate().getId())
                .fieldValues(extractFieldValues(doc))
                .build();

        return ruleEngine.suggestFields(context);
    }

    @Override
    @Transactional
    public void deleteDocument(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);
        doc.setDeleted(true);
        log.info("User id={} soft-deleted document id={}", currentUser.getId(), docId);
    }

    private Document findOwnedOrThrow(Long userId, Long docId) {
        return documentRepository.findByIdAndUserIdAndNotDeleted(docId, userId)
                .orElseThrow(() -> new NotFoundException("Document", docId));
    }

    private void validateRequiredTemplateFields(Document doc) {
        final var filledKeys = doc.getFieldValues().stream()
                .filter(fv -> fv.getFieldValue() != null && !fv.getFieldValue().isBlank())
                .map(DocumentFieldValue::getFieldKey)
                .collect(Collectors.toSet());

        final var missingLabels = doc.getTemplate().getTemplateFields().stream()
                .filter(kz.legeal.ease.backend.domain.TemplateField::isRequired)
                .filter(kz.legeal.ease.backend.domain.TemplateField::isActive)
                .filter(tf -> !filledKeys.contains(tf.getFieldKey()))
                .map(kz.legeal.ease.backend.domain.TemplateField::getLabel)
                .toList();

        if (!missingLabels.isEmpty()) {
            throw new BusinessException(
                    "Required fields are missing: " + String.join(", ", missingLabels),
                    "DOC_REQUIRED_FIELDS_MISSING"
            );
        }
    }

    private Map<String, String> extractFieldValues(Document doc) {
        return doc.getFieldValues().stream()
                .collect(Collectors.toMap(
                        DocumentFieldValue::getFieldKey,
                        DocumentFieldValue::getFieldValue
                ));
    }

    private String buildDocumentText(Document doc) {
        final var sb = new StringBuilder();
        sb.append("Документ: ").append(doc.getTitle()).append("\n");
        sb.append("Шаблон: ").append(doc.getTemplate().getTitle()).append("\n\n");
        doc.getFieldValues().forEach(fv ->
                sb.append(fv.getFieldKey()).append(": ").append(fv.getFieldValue()).append("\n")
        );
        return sb.toString();
    }

    private List<DocumentFieldValue> buildFieldValues(Map<String, String> values, Document doc) {
        if (values == null || values.isEmpty()) return new ArrayList<>();
        return values.entrySet().stream()
                .map(e -> DocumentFieldValue.builder()
                        .document(doc)
                        .fieldKey(e.getKey())
                        .fieldValue(e.getValue() != null ? e.getValue().trim() : "")
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void validateUnknownKeys(Map<String, String> values, Template template) {
        if (values == null || values.isEmpty()) return;

        final Set<String> validKeys = template.getTemplateFields().stream()
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

    private User requireCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}