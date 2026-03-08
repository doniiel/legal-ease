package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.RiskRule;
import kz.legeal.ease.backend.dto.RiskRuleDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RiskRuleMapper {

    RiskRuleDto toDto(RiskRule riskRule);
}
