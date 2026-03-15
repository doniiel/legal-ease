package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.RequiredDocRule;
import kz.legeal.ease.backend.dto.RequiredDocRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequiredDocRuleMapper {

    @Mapping(source = "template.id",    target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    RequiredDocRuleDto toDto(RequiredDocRule rule);
}
