package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.dto.document.DocumentPreviewDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "template.id", target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    @Mapping(source = "template.category.name", target = "categoryName")
    @Mapping(target = "missingRequiredFields", ignore = true)
    DocumentDto toDto(Document document);

    @AfterMapping
    default void computeMissingRequiredFields(Document document, @MappingTarget DocumentDto dto) {
        if (document.getTemplate() == null || document.getTemplate().getTemplateFields() == null) {
            dto.setMissingRequiredFields(Collections.emptyList());
            return;
        }

        Set<String> filledKeys = document.getFieldValues() == null
                ? Collections.emptySet()
                : document.getFieldValues().stream()
                        .filter(fv -> fv.getFieldValue() != null && !fv.getFieldValue().isBlank())
                        .map(fv -> fv.getFieldKey())
                        .collect(Collectors.toSet());

        List<String> missing = document.getTemplate().getTemplateFields().stream()
                .filter(f -> f.isRequired())
                .map(f -> f.getFieldKey())
                .filter(key -> !filledKeys.contains(key))
                .collect(Collectors.toList());

        dto.setMissingRequiredFields(missing);
    }

    @Mapping(source = "template.title", target = "templateTitle")
    @Mapping(source = "template.category.name", target = "categoryName")
    DocumentPreviewDto toPreviewDto(Document document);
}
