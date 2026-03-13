package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.ValidationRule;
import kz.legeal.ease.backend.dto.ValidationRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ValidationMapper {

    @Mapping(source = "template.id",    target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    ValidationRuleDto toDto(ValidationRule validationRule);
}