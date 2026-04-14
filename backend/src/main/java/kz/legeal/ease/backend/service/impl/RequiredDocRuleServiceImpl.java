package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.config.CacheConfig;
import kz.legeal.ease.backend.domain.RequiredDocRule;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.dto.RequiredDocRuleDto;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.RequiredDocRuleMapper;
import kz.legeal.ease.backend.repository.RequiredDocRuleRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.RequiredDocRuleRequest;
import kz.legeal.ease.backend.service.RequiredDocRuleService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequiredDocRuleServiceImpl implements RequiredDocRuleService {

    private final RequiredDocRuleRepository repository;
    private final TemplateRepository        templateRepository;
    private final RequiredDocRuleMapper     mapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.REQUIRED_DOC_RULES, key = "#templateId")
    public List<RequiredDocRuleDto> getAllByTemplate(Long templateId) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.REQUIRED_DOC_RULES, key = "#request.templateId")
    public RequiredDocRuleDto create(RequiredDocRuleRequest request) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var template = findTemplate(request.getTemplateId());
        final var rule = RequiredDocRule.builder()
                .template(template)
                .requiredDocTitle(request.getRequiredDocTitle())
                .reason(request.getReason())
                .conditionFieldKey(request.getConditionFieldKey())
                .conditionOperator(request.getConditionOperator())
                .conditionValue(request.getConditionValue())
                .mandatory(request.isMandatory())
                .active(true)
                .build();
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.REQUIRED_DOC_RULES, allEntries = true)
    public RequiredDocRuleDto update(Long id, RequiredDocRuleRequest request) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var rule     = findOrThrow(id);
        final var template = findTemplate(request.getTemplateId());

        rule.setTemplate(template);
        rule.setRequiredDocTitle(request.getRequiredDocTitle());
        rule.setReason(request.getReason());
        rule.setConditionFieldKey(request.getConditionFieldKey());
        rule.setConditionOperator(request.getConditionOperator());
        rule.setConditionValue(request.getConditionValue());
        rule.setMandatory(request.isMandatory());

        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.REQUIRED_DOC_RULES, allEntries = true)
    public void delete(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var rule = findOrThrow(id);
        rule.setActive(false);
        repository.save(rule);
    }

    private RequiredDocRule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequiredDocRule", id));
    }

    private Template findTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Template", id));
    }
}
