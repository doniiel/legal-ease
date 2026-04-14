package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.config.CacheConfig;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.ValidationRule;
import kz.legeal.ease.backend.dto.ValidationRuleDto;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.ValidationMapper;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.repository.ValidationRuleRepository;
import kz.legeal.ease.backend.request.ValidationRuleRequest;
import kz.legeal.ease.backend.service.ValidationRuleService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationRuleServiceImpl implements ValidationRuleService {

    private final ValidationRuleRepository repository;
    private final TemplateRepository       templateRepository;
    private final ValidationMapper         mapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.VALIDATION_RULES, key = "#templateId")
    public List<ValidationRuleDto> getAllByTemplate(Long templateId) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.VALIDATION_RULES, key = "#request.templateId")
    public ValidationRuleDto create(ValidationRuleRequest request) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var template = findTemplate(request.getTemplateId());
        final var rule = ValidationRule.builder()
                .template(template)
                .fieldKey(request.getFieldKey())
                .fieldLabel(request.getFieldLabel())
                .operator(request.getOperator())
                .expectedValue(request.getExpectedValue())
                .errorMessage(request.getErrorMessage())
                .active(true)
                .build();
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.VALIDATION_RULES, allEntries = true)
    public ValidationRuleDto update(Long id, ValidationRuleRequest request) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var rule = findOrThrow(id);

        rule.setFieldKey(request.getFieldKey());
        rule.setFieldLabel(request.getFieldLabel());
        rule.setOperator(request.getOperator());
        rule.setExpectedValue(request.getExpectedValue());
        rule.setErrorMessage(request.getErrorMessage());

        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.VALIDATION_RULES, allEntries = true)
    public void delete(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var rule = findOrThrow(id);
        rule.setActive(false);
        repository.save(rule);
    }

    private ValidationRule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ValidationRule", id));
    }

    private Template findTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Template", id));
    }
}
