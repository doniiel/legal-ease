package kz.legeal.ease.backend.service.lawyer.impl;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.domain.DocumentReview;
import kz.legeal.ease.backend.dto.document.DocumentReviewDto;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.DocumentReviewMapper;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.DocumentReviewRepository;
import kz.legeal.ease.backend.request.DocumentReviewRequest;
import kz.legeal.ease.backend.service.audit.AuditService;
import kz.legeal.ease.backend.service.lawyer.DocumentReviewService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentReviewServiceImpl implements DocumentReviewService {

    private final DocumentReviewRepository reviewRepository;
    private final DocumentRepository       documentRepository;
    private final DocumentReviewMapper     reviewMapper;
    private final AuditService             auditService;

    @Override
    @Transactional
    public DocumentReviewDto submitReview(Long documentId, DocumentReviewRequest request) {
        final var lawyer = currentLawyer();
        final var document = findDocument(documentId);

        assertLawyerOwnsTemplate(lawyer.getId(), document);

        // Update existing review if lawyer already reviewed this document, otherwise create new.
        final DocumentReview review;
        final var existing = reviewRepository.findByDocumentIdAndLawyerId(documentId, lawyer.getId());
        if (existing.isPresent()) {
            review = existing.get();
            review.setNotes(request.getNotes());
            review.setRiskLevel(request.getRiskLevel());
            review.setRecommended(request.isRecommended());
        } else {
            review = DocumentReview.builder()
                    .document(document)
                    .lawyer(lawyer)
                    .notes(request.getNotes())
                    .riskLevel(request.getRiskLevel())
                    .recommended(request.isRecommended())
                    .build();
        }

        final var saved = reviewRepository.save(review);
        auditService.log(lawyer.getId(), AuditAction.DOCUMENT_REVIEWED, "DocumentReview", saved.getId(), null);
        return reviewMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentReviewDto> getReviewsForDocument(Long documentId) {
        final var lawyer = currentLawyer();
        final var document = findDocument(documentId);
        assertLawyerOwnsTemplate(lawyer.getId(), document);
        return reviewRepository.findAllByDocumentIdOrderByCreatedDateDesc(documentId)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentReviewDto> getMyReviews(Pageable pageable) {
        final var lawyerId = currentLawyer().getId();
        return reviewRepository.findAllByLawyerId(lawyerId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        final var lawyerId = currentLawyer().getId();
        final var review = reviewRepository.findByIdAndLawyerId(reviewId, lawyerId)
                .orElseThrow(() -> new NotFoundException("DocumentReview", reviewId));
        reviewRepository.delete(review);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private kz.legeal.ease.backend.domain.User currentLawyer() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("Not authenticated"));
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new NotFoundException("Document", documentId));
    }

    private void assertLawyerOwnsTemplate(Long lawyerId, Document document) {
        if (!document.getTemplate().getLawyer().getId().equals(lawyerId)) {
            throw new BusinessRuleException(
                    "You may only review documents created from your own templates.",
                    "REVIEW_ACCESS_DENIED"
            );
        }
    }
}
