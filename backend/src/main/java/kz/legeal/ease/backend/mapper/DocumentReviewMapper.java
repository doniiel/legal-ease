package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.DocumentReview;
import kz.legeal.ease.backend.dto.document.DocumentReviewDto;
import kz.legeal.ease.backend.enums.ReviewStatus;
import kz.legeal.ease.backend.enums.RiskLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentReviewMapper {

    @Mapping(source = "document.id",    target = "documentId")
    @Mapping(source = "document.title", target = "documentTitle")
    @Mapping(source = "lawyer.id",      target = "lawyerInfo.id")
    @Mapping(source = "lawyer.fio",     target = "lawyerInfo.fio")
    @Mapping(source = "lawyer.email",   target = "lawyerInfo.email")
    @Mapping(source = "notes",          target = "comment")
    @Mapping(target = "status",         expression = "java(deriveStatus(review.isRecommended(), review.getRiskLevel()))")
    DocumentReviewDto toDto(DocumentReview review);

    default ReviewStatus deriveStatus(boolean recommended, RiskLevel riskLevel) {
        if (recommended) return ReviewStatus.APPROVED;
        if (riskLevel == RiskLevel.HIGH) return ReviewStatus.REJECTED;
        return ReviewStatus.NEEDS_REVISION;
    }
}
