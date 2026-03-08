package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.ConditionalRuleDto;
import kz.legeal.ease.backend.request.ConditionalRuleRequest;

import java.util.List;

public interface ConditionalRuleService {

    List<ConditionalRuleDto> getAllByTemplate(Long templateId);

    ConditionalRuleDto create(ConditionalRuleRequest request);

    ConditionalRuleDto update(Long id, ConditionalRuleRequest request);

    void delete(Long id);
}
