package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.config.CacheConfig;
import kz.legeal.ease.backend.domain.ConditionRule;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.dto.ConditionalRuleDto;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.ConditionalRuleMapper;
import kz.legeal.ease.backend.repository.ConditionalRuleRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.ConditionalRuleRequest;
import kz.legeal.ease.backend.service.ConditionalRuleService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConditionRuleServiceImpl implements ConditionalRuleService {

    private final ConditionalRuleRepository repository;
    private final TemplateRepository        templateRepository;
    private final ConditionalRuleMapper     mapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CONDITIONAL_RULES, key = "#templateId")
    public List<ConditionalRuleDto> getAllByTemplate(Long templateId) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONDITIONAL_RULES, key = "#request.templateId")
    public ConditionalRuleDto create(ConditionalRuleRequest request) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var template = findTemplate(request.getTemplateId());
        final var rule = ConditionRule.builder()
                .template(template)
                .conditionFieldKey(request.getConditionFieldKey())
                .operator(request.getOperator())
                .conditionValue(request.getConditionValue())
                .targetFieldKey(request.getTargetFieldKey())
                .active(true)
                .build();
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONDITIONAL_RULES, allEntries = true)
    public ConditionalRuleDto update(Long id, ConditionalRuleRequest request) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var rule     = findOrThrow(id);
        final var template = findTemplate(request.getTemplateId());

        rule.setTemplate(template);
        rule.setConditionFieldKey(request.getConditionFieldKey());
        rule.setOperator(request.getOperator());
        rule.setConditionValue(request.getConditionValue());
        rule.setTargetFieldKey(request.getTargetFieldKey());

        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONDITIONAL_RULES, allEntries = true)
    public void delete(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "LAWYER");
        final var rule = findOrThrow(id);
        rule.setActive(false);
        repository.save(rule);
    }

    private ConditionRule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ConditionRule", id));
    }

    private Template findTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Template", id));
    }
}
