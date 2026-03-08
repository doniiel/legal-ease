package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.ValidationRule;
import kz.legeal.ease.backend.dto.ValidationRuleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ValidationMapper {

    ValidationRuleDto toDto(ValidationRule validationRule);
}
