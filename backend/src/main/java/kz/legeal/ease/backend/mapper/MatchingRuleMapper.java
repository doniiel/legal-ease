package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.MatchingRule;
import kz.legeal.ease.backend.dto.MatchingRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchingRuleMapper {

    @Mapping(source = "template.id",    target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    @Mapping(source = "category.id",    target = "categoryId")
    @Mapping(source = "category.name",  target = "categoryName")
    MatchingRuleDto toDto(MatchingRule matchingRule);
}