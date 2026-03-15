package kz.legeal.ease.backend.service.lawyer.impl;

import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.DocumentMapper;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.service.lawyer.LawyerDocumentService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LawyerDocumentServiceImpl implements LawyerDocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper     documentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentPreviewDto> getDocumentsByMyTemplates(Pageable pageable) {
        final var lawyer = currentLawyerId();
        return documentRepository.findAllByTemplateOwnerLawyerId(lawyer, pageable)
                .map(documentMapper::toPreviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentPreviewDto> getDocumentsByTemplate(Long templateId, Pageable pageable) {
        final var lawyer = currentLawyerId();
        return documentRepository.findAllByTemplateIdAndLawyerId(templateId, lawyer, pageable)
                .map(documentMapper::toPreviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getDocumentById(Long documentId) {
        final var lawyer = currentLawyerId();
        final var doc = documentRepository.findById(documentId)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new NotFoundException("Document", documentId));

        if (!doc.getTemplate().getLawyer().getId().equals(lawyer)) {
            throw new BusinessRuleException(
                    "You do not own the template this document was created from.",
                    "DOCUMENT_ACCESS_DENIED"
            );
        }
        return documentMapper.toDto(doc);
    }

    private Long currentLawyerId() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("Not authenticated"))
                .getId();
    }
}
