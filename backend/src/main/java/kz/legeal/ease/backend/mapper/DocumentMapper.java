package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "template.id", target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    @Mapping(source = "template.category.name", target = "categoryName")
    @Mapping(target = "missingRequiredFields", ignore = true)
    DocumentDto toDto(Document document);

    @Mapping(source = "template.title", target = "templateTitle")
    @Mapping(source = "template.category.name", target = "categoryName")
    DocumentPreviewDto toPreviewDto(Document document);
}