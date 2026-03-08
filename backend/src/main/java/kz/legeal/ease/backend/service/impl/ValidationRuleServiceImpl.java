package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.ValidationRule;
import kz.legeal.ease.backend.dto.ValidationRuleDto;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.ValidationMapper;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.repository.ValidationRuleRepository;
import kz.legeal.ease.backend.request.ValidationRuleRequest;
import kz.legeal.ease.backend.service.ValidationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationRuleServiceImpl implements ValidationRuleService {

    private final ValidationRuleRepository repository;
    private final TemplateRepository templateRepository;
    private final ValidationMapper mapper;

    @Override
    @Transactional
    public List<ValidationRuleDto> getAllByTemplate(Long templateId) {
        return repository.findAllByTemplateIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ValidationRuleDto create(ValidationRuleRequest request) {
        final var template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException(Template.class.getName(), request.getTemplateId()));

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
    public ValidationRuleDto update(Long id, ValidationRuleRequest request) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ValidationRule.class.getName(), id));

        rule.setFieldKey(request.getFieldKey());
        rule.setFieldLabel(request.getFieldLabel());
        rule.setOperator(request.getOperator());
        rule.setExpectedValue(request.getExpectedValue());
        rule.setErrorMessage(request.getErrorMessage());
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ValidationRule.class.getName(), id));

        rule.setActive(false);
        repository.save(rule);
    }
}
