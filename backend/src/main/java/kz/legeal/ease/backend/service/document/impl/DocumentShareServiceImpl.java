package kz.legeal.ease.backend.service.document.impl;

import kz.legeal.ease.backend.domain.DocumentShare;
import kz.legeal.ease.backend.dto.document.DocumentShareResponse;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.enums.DocumentStatus;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.DocumentShareRepository;
import kz.legeal.ease.backend.service.audit.AuditService;
import kz.legeal.ease.backend.service.document.DocumentShareService;
import kz.legeal.ease.backend.storage.StorageService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentShareServiceImpl implements DocumentShareService {

    private static final int TOKEN_BYTES = 32; // 256-bit → 64 hex chars

    private final DocumentRepository      documentRepository;
    private final DocumentShareRepository documentShareRepository;
    private final StorageService          storageService;
    private final AuditService            auditService;

    @Value("${app.share.base-url:http://localhost:9191}")
    private String baseUrl;

    @Value("${app.share.expiry-hours:24}")
    private int expiryHours;

    @Override
    @Transactional
    public DocumentShareResponse createShareLink(Long docId) {
        final var currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        final var doc = documentRepository.findByIdAndUserIdAndNotDeleted(docId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Document", docId));

        if (doc.getStatus() != DocumentStatus.COMPLETED || doc.getS3ObjectKey() == null) {
            throw new BusinessRuleException(
                    "Only completed documents with a stored PDF can be shared.",
                    "DOC_NOT_SHAREABLE"
            );
        }

        final String token    = generateToken();
        final var    expiresAt = LocalDateTime.now().plusHours(expiryHours);

        final var share = DocumentShare.builder()
                .document(doc)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        documentShareRepository.save(share);

        final String shareUrl = baseUrl + "/api/public/share/" + token;
        log.info("User id={} shared document id={} → token={}", currentUser.getId(), docId, token);
        auditService.log(currentUser.getId(), AuditAction.DOCUMENT_SHARED, "Document", docId,
                "{\"token\":\"" + token + "\"}");
        return new DocumentShareResponse(shareUrl, token, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] resolveShare(String token) {
        final var share = documentShareRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("Share link not found or has been revoked.", "SHARE_NOT_FOUND"));

        if (LocalDateTime.now().isAfter(share.getExpiresAt())) {
            throw new BusinessRuleException("This share link has expired.", "SHARE_EXPIRED");
        }

        final var doc = share.getDocument();
        if (doc.getS3ObjectKey() == null) {
            throw new BusinessRuleException("Document PDF is not available.", "DOC_NO_PDF");
        }

        log.info("Public share download for token={} document id={}", token, doc.getId());
        return storageService.downloadFile(doc.getS3ObjectKey());
    }

    private String generateToken() {
        final var random = new SecureRandom();
        final byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
