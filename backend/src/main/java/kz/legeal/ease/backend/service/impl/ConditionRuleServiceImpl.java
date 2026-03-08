package kz.legeal.ease.backend.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConditionRuleServiceImpl implements ConditionalRuleService {

    private final ConditionalRuleRepository repository;
    private final TemplateRepository templateRepository;
    private final ConditionalRuleMapper mapper;

    @Override
    @Transactional
    public List<ConditionalRuleDto> getAllByTemplate(Long templateId) {
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ConditionalRuleDto create(ConditionalRuleRequest request) {
        final var template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException(Template.class.getName(), request.getTemplateId()));

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
        final var template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException(Template.class.getName(), request.getTemplateId()));

        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ConditionRule.class.getName(), id));

        rule.setConditionFieldKey(request.getConditionFieldKey());
        rule.setOperator(request.getOperator());
        rule.setTemplate(template);
        rule.setConditionValue(request.getConditionValue());
        rule.setTargetFieldKey(request.getTargetFieldKey());
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ConditionRule.class.getName(), id));
        rule.setActive(false);
        repository.save(rule);
    }
}
