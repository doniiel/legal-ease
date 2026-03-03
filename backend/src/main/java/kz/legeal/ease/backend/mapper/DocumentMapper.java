package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    DocumentDto toDto(Document document);

    DocumentPreviewDto toPreviewDto(Document document);
}
