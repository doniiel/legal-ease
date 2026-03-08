package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.RiskRule;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.dto.RiskRuleDto;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.RiskRuleMapper;
import kz.legeal.ease.backend.repository.RiskRuleRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.RiskRuleRequest;
import kz.legeal.ease.backend.service.RiskRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskRuleServiceImpl implements RiskRuleService {

    private final RiskRuleRepository repository;
    private final TemplateRepository templateRepository;
    private final RiskRuleMapper mapper;

    @Override
    @Transactional
    public List<RiskRuleDto> getAllByTemplate(Long templateId) {
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RiskRuleDto create(RiskRuleRequest request) {
        final var template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException(Template.class.getName(), request.getTemplateId()));

        final var rule = RiskRule.builder()
                .template(template)
                .ruleCode(request.getRuleCode())
                .fieldKey(request.getFieldKey())
                .operator(request.getOperator())
                .expectedValue(request.getExpectedValue())
                .riskMessage(request.getRiskMessage())
                .riskLevel(request.getRiskLevel())
                .active(true)
                .build();

        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public RiskRuleDto update(Long id, RiskRuleRequest request) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(RiskRule.class.getName(), id));

        rule.setRuleCode(request.getRuleCode());
        rule.setFieldKey(request.getFieldKey());
        rule.setOperator(request.getOperator());
        rule.setExpectedValue(request.getExpectedValue());
        rule.setRiskMessage(request.getRiskMessage());
        rule.setRiskLevel(request.getRiskLevel());
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(RiskRule.class.getName(), id));
        rule.setActive(false);
        repository.save(rule);
    }
}
