package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.TemplateField;
import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.dto.template.TemplateFieldDto;
import kz.legeal.ease.backend.dto.template.TemplatePreviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TemplateMapper {

    @Mapping(target = "fields", source = "templateFields")
    @Mapping(target = "lawyer.id", source = "lawyer.id")
    @Mapping(target = "lawyer.email", source = "lawyer.email")
    @Mapping(target = "lawyer.fio", source = "lawyer.fio")
    TemplateDto toDto(Template template);

    List<TemplateDto> toDtoList(List<Template> templates);

    @Mapping(target = "lawyerName", source = "lawyer.fio")
    @Mapping(target = "fieldCount", expression = "java(template.getTemplateFields() != null ? (int) template.getTemplateFields().stream().filter(f -> f.isActive()).count() : 0)")
    TemplatePreviewDto toPreviewDto(Template template);

    TemplateFieldDto toFieldDto(TemplateField field);

    List<TemplateFieldDto> toFieldDtoList(List<TemplateField> fields);
}
