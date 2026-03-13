package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.RiskRule;
import kz.legeal.ease.backend.dto.RiskRuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskRuleMapper {

    // ✅ Явно маппим вложенные поля template.id → templateId, template.title → templateTitle
    @Mapping(source = "template.id", target = "templateId")
    @Mapping(source = "template.title", target = "templateTitle")
    RiskRuleDto toDto(RiskRule riskRule);
}
