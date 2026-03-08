package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.ValidationRuleDto;
import kz.legeal.ease.backend.request.ValidationRuleRequest;

import java.util.List;

public interface ValidationRuleService {

    List<ValidationRuleDto> getAllByTemplate(Long templateId);

    ValidationRuleDto create(ValidationRuleRequest request);

    ValidationRuleDto update(Long id, ValidationRuleRequest request);

    void delete(Long id);
}
