package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.ConditionRule;
import kz.legeal.ease.backend.dto.ConditionalRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConditionalRuleMapper {

    @Mapping(source = "template.id",    target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    @Mapping(source = "conditionFieldKey", target = "conditionalFieldKey")
    @Mapping(source = "conditionValue",    target = "conditionalValue")
    ConditionalRuleDto toDto(ConditionRule rule);
}