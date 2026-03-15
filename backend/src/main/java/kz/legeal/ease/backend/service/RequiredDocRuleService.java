package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.RequiredDocRuleDto;
import kz.legeal.ease.backend.request.RequiredDocRuleRequest;

import java.util.List;

public interface RequiredDocRuleService {

    List<RequiredDocRuleDto> getAllByTemplate(Long templateId);

    RequiredDocRuleDto create(RequiredDocRuleRequest request);

    RequiredDocRuleDto update(Long id, RequiredDocRuleRequest request);

    void delete(Long id);
}
