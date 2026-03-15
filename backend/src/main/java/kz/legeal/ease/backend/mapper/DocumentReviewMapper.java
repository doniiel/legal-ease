package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.DocumentReview;
import kz.legeal.ease.backend.dto.document.DocumentReviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentReviewMapper {

    @Mapping(source = "document.id",    target = "documentId")
    @Mapping(source = "document.title", target = "documentTitle")
    @Mapping(source = "lawyer.id",      target = "lawyerId")
    @Mapping(source = "lawyer.email",   target = "lawyerEmail")
    DocumentReviewDto toDto(DocumentReview review);
}
