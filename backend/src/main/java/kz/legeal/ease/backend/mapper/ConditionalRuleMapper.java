package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.ConditionRule;
import kz.legeal.ease.backend.dto.ConditionalRuleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConditionalRuleMapper {

    ConditionalRuleDto toDto(ConditionRule rule);
}
