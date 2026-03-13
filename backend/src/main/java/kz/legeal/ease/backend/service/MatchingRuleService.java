package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.MatchingRuleDto;
import kz.legeal.ease.backend.request.MatchingRuleRequest;

import java.util.List;

public interface MatchingRuleService {

    List<MatchingRuleDto> getAll();

    List<MatchingRuleDto> getAllByTemplate(Long templateId);

    MatchingRuleDto create(MatchingRuleRequest request);

    MatchingRuleDto update(Long id, MatchingRuleRequest request);

    void delete(Long id);
}
