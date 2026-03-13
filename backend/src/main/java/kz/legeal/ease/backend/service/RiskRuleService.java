package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.RiskRuleDto;
import kz.legeal.ease.backend.request.RiskRuleRequest;

import java.util.List;

public interface RiskRuleService {

    List<RiskRuleDto> getAllByTemplate(Long templateId);

    RiskRuleDto create(RiskRuleRequest request);

    RiskRuleDto update(Long id, RiskRuleRequest request);

    void delete(Long id);
}
