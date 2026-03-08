package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.MatchingRule;
import kz.legeal.ease.backend.dto.MatchingRuleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchingRuleMapper {

    MatchingRuleDto toDto(MatchingRule matchingRule);
}
