package kz.legeal.ease.backend.service.document.impl;

import kz.legeal.ease.backend.config.S3Properties;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.DocumentFieldValue;
import kz.legeal.ease.backend.domain.DocumentVersion;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.CompleteDocumentResponseDto;
import kz.legeal.ease.backend.dto.ai.DocumentExplainResponse;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.dto.document.DocumentVersionDto;
import kz.legeal.ease.backend.dto.document.PresignedUrlResponse;
import kz.legeal.ease.backend.enums.DocumentStatus;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.ForbiddenException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.DocumentMapper;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.DocumentShareRepository;
import kz.legeal.ease.backend.repository.DocumentVersionRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.document.CreateDocumentRequest;
import kz.legeal.ease.backend.request.document.UpdateDocumentRequest;
import kz.legeal.ease.backend.service.RateLimitService;
import kz.legeal.ease.backend.service.audit.AuditService;
import kz.legeal.ease.backend.service.document.DocumentPdfService;
import kz.legeal.ease.backend.service.document.DocumentService;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.context.RuleContext;
import kz.legeal.ease.backend.service.rule.engine.RuleEngine;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import kz.legeal.ease.backend.storage.StorageService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository        documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentShareRepository   documentShareRepository;
    private final TemplateRepository        templateRepository;
    private final DocumentMapper            documentMapper;
    private final RuleEngine                ruleEngine;
    private final DocumentPdfService        pdfService;
    private final StorageService            storageService;
    private final S3Properties              s3Properties;
    private final AuditService              auditService;
    private final RateLimitService          rateLimitService;
    private final EntityManager             entityManager;
    private final RuleAiService             aiService;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest request) {
        final var currentUser = requireCurrentUser();

        final var template = templateRepository.findByIdAndActive(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException("Template", request.getTemplateId()));

        if (!template.isPublished()) {
            throw new BusinessRuleException("Cannot create document from unpublished template", "TPL_NOT_PUBLISHED");
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
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_CREATED, "Document", saved.getId(), null);
        return documentMapper.toDto(saved);
    }

    // ─── READ ────────────────────────────────────────────────────────────────

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
    public Page<DocumentPreviewDto> getMyDocumentsIncludingArchived(Pageable pageable) {
        final var currentUser = requireCurrentUser();
        return documentRepository
                .findAllByUserIdIncludingArchived(currentUser.getId(), pageable)
                .map(documentMapper::toPreviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getMyDocumentById(Long docId) {
        final var currentUser = requireCurrentUser();
        final var document = findOwnedOrThrow(currentUser.getId(), docId);
        return documentMapper.toDto(document);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentDto update(Long docId, UpdateDocumentRequest req) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() != DocumentStatus.DRAFT && doc.getStatus() != DocumentStatus.VALIDATED) {
            throw new BusinessRuleException(
                    "Only DRAFT or VALIDATED documents can be edited. Archive or restore first if needed.",
                    "DOC_NOT_EDITABLE"
            );
        }

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            doc.setTitle(req.getTitle());
        }

        if (req.getFieldValues() != null) {
            validateUnknownKeys(req.getFieldValues(), doc.getTemplate());
            doc.getFieldValues().clear();
            entityManager.flush(); // force DELETEs before INSERTs to avoid uq_document_field_key violation
            final var newValues = buildFieldValues(req.getFieldValues(), doc);
            doc.getFieldValues().addAll(newValues);
            // Editing field values invalidates any prior VALIDATED state
            if (doc.getStatus() == DocumentStatus.VALIDATED) {
                doc.setStatus(DocumentStatus.DRAFT);
            }
        }

        log.info("User id={} updated document id={}", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_UPDATED, "Document", docId, null);
        return documentMapper.toDto(doc);
    }

    // ─── VALIDATE ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RuleEngineResult validate(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() == DocumentStatus.COMPLETED) {
            throw new BusinessRuleException("COMPLETED documents cannot be re-validated.", "DOC_ALREADY_COMPLETED");
        }
        if (doc.getStatus() == DocumentStatus.ARCHIVED) {
            throw new BusinessRuleException("Restore the document before validating.", "DOC_ARCHIVED");
        }

        validateRequiredTemplateFields(doc);

        final var context = RuleContext.builder()
                .templateId(doc.getTemplate().getId())
                .fieldValues(extractFieldValues(doc))
                .documentText(buildDocumentText(doc))
                .build();

        final var result = ruleEngine.complete(context);

        if (result.isValid()) {
            doc.setStatus(DocumentStatus.VALIDATED);
            log.info("Document id={} passed validation → status=VALIDATED", docId);
            auditService.log(currentUser.getId(), AuditAction.DOCUMENT_VALIDATED, "Document", docId, null);
        } else {
            // Keep status as DRAFT so the user can fix and retry
            doc.setStatus(DocumentStatus.DRAFT);
            log.info("Document id={} failed validation: {} errors", docId, result.getValidationErrors().size());
        }

        return result;
    }

    // ─── COMPLETE ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CompleteDocumentResponseDto complete(Long docId) {
        final var currentUser = requireCurrentUser();
        rateLimitService.checkCompleteLimit(currentUser.getId());

        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() == DocumentStatus.ARCHIVED) {
            throw new BusinessRuleException("Restore the document before completing.", "DOC_ARCHIVED");
        }

        // Allow completing from DRAFT (runs validation inline) or VALIDATED
        if (doc.getStatus() == DocumentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Document is already completed. Use versioning to create a new version after editing.",
                    "DOC_ALREADY_COMPLETED"
            );
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
            doc.setStatus(DocumentStatus.DRAFT);
            return CompleteDocumentResponseDto.failed(ruleResult);
        }

        doc.setStatus(DocumentStatus.COMPLETED);

        try {
            final int nextVersion  = doc.getCurrentVersion() + 1;
            final byte[] pdfBytes  = pdfService.generate(doc);
            doc.setContentHash(sha256Hex(pdfBytes));
            final String objectKey = buildVersionedObjectKey(currentUser.getId(), docId, nextVersion);
            storageService.uploadFile(objectKey, pdfBytes, "application/pdf");

            final var docVersion = DocumentVersion.builder()
                    .document(doc)
                    .version(nextVersion)
                    .s3ObjectKey(objectKey)
                    .build();
            documentVersionRepository.save(docVersion);

            doc.setCurrentVersion(nextVersion);
            doc.setS3ObjectKey(objectKey);
            log.info("PDF v{} uploaded for document id={} → key={}", nextVersion, docId, objectKey);
        } catch (RuntimeException e) {
            log.error("PDF generation/upload failed for document id={}: {}", docId, e.getMessage(), e);
            throw e; // rolls back @Transactional — document status stays unchanged in DB
        }

        log.info("User id={} completed document id={}", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_COMPLETED, "Document", docId, null);
        return CompleteDocumentResponseDto.success(documentMapper.toDto(doc), ruleResult);
    }

    // ─── SUGGESTIONS & AI ANALYSIS ───────────────────────────────────────────

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
    @Transactional(readOnly = true)
    public RuleEngineResult analyzeWithAi(Long docId) {
        final var currentUser = requireCurrentUser();
        rateLimitService.checkAiLimit(currentUser.getId());

        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        final var context = RuleContext.builder()
                .templateId(doc.getTemplate().getId())
                .fieldValues(extractFieldValues(doc))
                .documentText(buildDocumentText(doc))
                .build();

        final var result = ruleEngine.complete(context);
        log.info("User id={} ran AI analysis on document id={}", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_AI_ANALYZED, "Document", docId, null);
        return result;
    }

    // ─── EXPLAIN ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DocumentExplainResponse explainDocument(Long docId) {
        final var currentUser = requireCurrentUser();
        rateLimitService.checkAiLimit(currentUser.getId());

        final var doc = findOwnedOrThrow(currentUser.getId(), docId);
        final var documentText = buildDocumentText(doc);
        final var templateTitle = doc.getTemplate().getTitle();

        log.info("User id={} requested explanation for document id={}", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_AI_ANALYZED, "Document", docId, null);
        return aiService.explainDocument(documentText, templateTitle);
    }

    // ─── ARCHIVE / RESTORE ───────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentDto archive(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() == DocumentStatus.ARCHIVED) {
            throw new BusinessRuleException("Document is already archived.", "DOC_ALREADY_ARCHIVED");
        }
        if (doc.getStatus() == DocumentStatus.DRAFT || doc.getStatus() == DocumentStatus.VALIDATED) {
            throw new BusinessRuleException(
                    "Only COMPLETED documents can be archived. Complete the document first.",
                    "DOC_NOT_COMPLETED"
            );
        }

        doc.setStatus(DocumentStatus.ARCHIVED);
        // Revoke all active share links for this document
        final int revokedShares = documentShareRepository.deleteAllByDocumentId(docId);
        if (revokedShares > 0) {
            log.info("Revoked {} share link(s) for archived document id={}", revokedShares, docId);
        }

        log.info("User id={} archived document id={}", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_ARCHIVED, "Document", docId, null);
        return documentMapper.toDto(doc);
    }

    @Override
    @Transactional
    public DocumentDto restore(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() != DocumentStatus.ARCHIVED) {
            throw new BusinessRuleException(
                    "Only ARCHIVED documents can be restored. Current status: " + doc.getStatus(),
                    "DOC_NOT_ARCHIVED"
            );
        }

        doc.setStatus(DocumentStatus.DRAFT);
        log.info("User id={} restored document id={} → status=DRAFT", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_RESTORED, "Document", docId, null);
        return documentMapper.toDto(doc);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteDocument(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() == DocumentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "COMPLETED documents cannot be deleted directly. Archive the document first.",
                    "DOC_DELETE_COMPLETED"
            );
        }

        doc.setDeleted(true);
        documentShareRepository.deleteAllByDocumentId(docId);
        log.info("User id={} soft-deleted document id={}", currentUser.getId(), docId);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_DELETED, "Document", docId, null);
    }

    // ─── REGENERATE PDF ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentDto regeneratePdf(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getStatus() != DocumentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Only COMPLETED documents can have their PDF regenerated.",
                    "DOC_NOT_COMPLETED"
            );
        }

        try {
            final int nextVersion  = doc.getCurrentVersion() + 1;
            final byte[] pdfBytes  = pdfService.generate(doc);
            doc.setContentHash(sha256Hex(pdfBytes));
            final String objectKey = buildVersionedObjectKey(currentUser.getId(), docId, nextVersion);
            storageService.uploadFile(objectKey, pdfBytes, "application/pdf");

            final var docVersion = DocumentVersion.builder()
                    .document(doc)
                    .version(nextVersion)
                    .s3ObjectKey(objectKey)
                    .build();
            documentVersionRepository.save(docVersion);

            doc.setCurrentVersion(nextVersion);
            doc.setS3ObjectKey(objectKey);
            log.info("PDF regenerated: v{} for document id={} → key={}", nextVersion, docId, objectKey);
        } catch (RuntimeException e) {
            log.error("PDF regeneration failed for document id={}: {}", docId, e.getMessage(), e);
            throw e;
        }

        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_COMPLETED, "Document", docId,
                "{\"regenerated\":true}");
        return documentMapper.toDto(doc);
    }

    // ─── DOWNLOAD ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getS3ObjectKey() == null) {
            throw new BusinessRuleException(
                    "Document has no stored PDF. Complete the document first.",
                    "DOC_NO_PDF"
            );
        }
        final byte[] bytes = storageService.downloadFile(doc.getS3ObjectKey());
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_DOWNLOADED, "Document", docId, null);
        return bytes;
    }

    @Override
    @Transactional(readOnly = true)
    public PresignedUrlResponse getPresignedUrl(Long docId) {
        final var currentUser = requireCurrentUser();
        final var doc = findOwnedOrThrow(currentUser.getId(), docId);

        if (doc.getS3ObjectKey() == null) {
            throw new BusinessRuleException(
                    "Document has no stored PDF. Complete the document first.",
                    "DOC_NO_PDF"
            );
        }

        final int expiryMinutes = s3Properties.getPresignExpiryMinutes();
        final String url = storageService.generatePresignedUrl(
                doc.getS3ObjectKey(),
                Duration.ofMinutes(expiryMinutes)
        );
        return new PresignedUrlResponse(url, expiryMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentVersionDto> getVersions(Long docId) {
        final var currentUser = requireCurrentUser();
        findOwnedOrThrow(currentUser.getId(), docId);
        return documentVersionRepository.findAllByDocumentIdOrderByVersionAsc(docId)
                .stream()
                .map(v -> new DocumentVersionDto(v.getId(), v.getVersion(), v.getS3ObjectKey(),
                        v.getCreatedDate(), v.getCreatedBy()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadVersion(Long docId, int version) {
        final var currentUser = requireCurrentUser();
        findOwnedOrThrow(currentUser.getId(), docId);
        final var docVersion = documentVersionRepository.findByDocumentIdAndVersion(docId, version)
                .orElseThrow(() -> new NotFoundException("DocumentVersion", (long) version));
        final byte[] bytes = storageService.downloadFile(docVersion.getS3ObjectKey());
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_VERSION_DOWNLOADED, "Document", docId,
                "{\"version\":" + version + "}");
        return bytes;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

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
            throw new BusinessRuleException(
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
        final var labelMap = doc.getTemplate().getTemplateFields().stream()
                .collect(Collectors.toMap(
                        kz.legeal.ease.backend.domain.TemplateField::getFieldKey,
                        kz.legeal.ease.backend.domain.TemplateField::getLabel
                ));
        final var sb = new StringBuilder();
        sb.append("Документ: ").append(doc.getTitle()).append("\n");
        sb.append("Шаблон: ").append(doc.getTemplate().getTitle()).append("\n\n");
        doc.getFieldValues().forEach(fv -> {
            final var label = labelMap.getOrDefault(fv.getFieldKey(), fv.getFieldKey());
            sb.append(label).append(": ").append(fv.getFieldValue()).append("\n");
        });
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
            throw new BusinessRuleException(
                    "Unknown field keys: " + String.join(", ", unknownKeys),
                    "DOC_UNKNOWN_FIELDS"
            );
        }
    }

    private User requireCurrentUser() {
        final var user = SecurityUtils.requireCurrentUser();
        SecurityUtils.requireAnyRole(user, "USER", "ADMIN");
        return user;
    }

    private String buildVersionedObjectKey(Long userId, Long docId, int version) {
        return "documents/%d/%d/v%d.pdf".formatted(userId, docId, version);
    }

    /** Computes a hex-encoded SHA-256 digest of the given bytes. */
    private static String sha256Hex(byte[] data) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }
}
