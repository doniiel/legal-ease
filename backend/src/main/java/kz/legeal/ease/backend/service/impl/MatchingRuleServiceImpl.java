package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.Category;
import kz.legeal.ease.backend.domain.MatchingRule;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.dto.MatchingRuleDto;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.MatchingRuleMapper;
import kz.legeal.ease.backend.repository.CategoryRepository;
import kz.legeal.ease.backend.repository.MatchingRuleRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.MatchingRuleRequest;
import kz.legeal.ease.backend.service.MatchingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingRuleServiceImpl implements MatchingRuleService {

    private final MatchingRuleRepository repository;
    private final TemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;
    private final MatchingRuleMapper mapper;

    @Override
    @Transactional
    public List<MatchingRuleDto> getAll() {
        return repository.findAllByActiveTrue().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<MatchingRuleDto> getAllByTemplate(Long templateId) {
        return repository.findAllByCategoryIdAndActiveTrue(templateId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MatchingRuleDto create(MatchingRuleRequest request) {
        final var template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException(Template.class.getName(), request.getTemplateId()));

        final var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(Category.class.getName(), request.getCategoryId()));

        final var rule = MatchingRule.builder()
                .template(template)
                .category(category)
                .keywords(request.getKeywords())
                .baseScore(request.getBaseScore() != null ? request.getBaseScore() : 50)
                .active(true)
                .build();

        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public MatchingRuleDto update(Long id, MatchingRuleRequest request) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(MatchingRule.class.getName(), id));

        final var template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException(Template.class.getName(), request.getTemplateId()));

        final var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(Category.class.getName(), request.getCategoryId()));

        rule.setKeywords(request.getKeywords());
        rule.setBaseScore(request.getBaseScore() != null ? request.getBaseScore() : rule.getBaseScore());
        rule.setTemplate(template);
        rule.setCategory(category);
        return mapper.toDto(repository.save(rule));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var rule = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(MatchingRule.class.getName(), id));

        rule.setActive(false);
        repository.save(rule);
    }
}
