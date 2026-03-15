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
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONDITIONAL_RULES, key = "#request.templateId")
    public ConditionalRuleDto create(ConditionalRuleRequest request) {
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
    public ConditionalRuleDto update(Long id, ConditionalRuleRequest request) {
        final var rule     = findOrThrow(id);
        final var template = findTemplate(request.getTemplateId());
        evictCache(rule.getTemplate().getId());

        rule.setTemplate(template);
        rule.setConditionFieldKey(request.getConditionFieldKey());
        rule.setOperator(request.getOperator());
        rule.setConditionValue(request.getConditionValue());
        rule.setTargetFieldKey(request.getTargetFieldKey());

        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var rule = findOrThrow(id);
        evictCache(rule.getTemplate().getId());
        rule.setActive(false);
        repository.save(rule);
    }

    @CacheEvict(value = CacheConfig.CONDITIONAL_RULES, key = "#templateId")
    public void evictCache(Long templateId) {}

    private ConditionRule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ConditionRule", id));
    }

    private Template findTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Template", id));
    }
}
