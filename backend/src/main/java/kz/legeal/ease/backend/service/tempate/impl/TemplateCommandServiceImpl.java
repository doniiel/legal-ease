package kz.legeal.ease.backend.service.tempate.impl;

import kz.legeal.ease.backend.domain.Category;
import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.domain.TemplateField;
import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.ForbiddenException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.exception.UnauthorizedException;
import kz.legeal.ease.backend.mapper.TemplateMapper;
import kz.legeal.ease.backend.repository.CategoryRepository;
import kz.legeal.ease.backend.repository.TemplateFieldRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.request.TemplateFieldRequest;
import kz.legeal.ease.backend.request.TemplateRequest;
import kz.legeal.ease.backend.service.tempate.TemplateCommandService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateCommandServiceImpl implements TemplateCommandService {

    private final TemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final CategoryRepository categoryRepository;
    private final TemplateMapper templateMapper;

    @Override
    @Transactional
    public TemplateDto create(TemplateRequest request) {
        final var currentLawyer = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        final var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(Category.class.getName(), request.getCategoryId()));

        final var template = Template.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .lawyer(currentLawyer)
                .build();

        if (request.getFields() != null && !request.getFields().isEmpty()) {
            final var fields = buildFields(request.getFields(), template, currentLawyer.getEmail());
            template.getTemplateFields().addAll(fields);
        }

        return templateMapper.toDto(templateRepository.save(template));
    }

    @Override
    @Transactional
    public TemplateDto update(Long templateId, TemplateRequest request) {
        final var currentLawyer = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        final var template = getOwnedDraftTemplate(templateId, currentLawyer.getId());
        final var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(Category.class.getName(), request.getCategoryId()));

        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());
        template.setCategory(category);

        template.getTemplateFields().clear();
        templateFieldRepository.flush();

        if (request.getFields() != null && !request.getFields().isEmpty()) {
            final var newFields = buildFields(request.getFields(), template, currentLawyer.getEmail());
            template.getTemplateFields().addAll(newFields);
        }

        return templateMapper.toDto(templateRepository.save(template));
    }

    @Override
    @Transactional
    public TemplateDto publish(Long templateId) {
        final var currentLawyer = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        final var template = getOwnedTemplate(templateId, currentLawyer.getId());

        if (template.isPublished()) {
            throw new BusinessRuleException(
                    "Template id=" + templateId + " is already published", "TPL_001");
        }

        template.publish();
        return templateMapper.toDto(templateRepository.save(template));
    }

    @Override
    @Transactional
    public void delete(Long templateId) {
        final var currentLawyer = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        final var template = getOwnedDraftTemplate(templateId, currentLawyer.getId());
        template.setActive(false);
        templateRepository.save(template);
    }

    private Template getOwnedTemplate(Long templateId, Long lawyerId) {
        return templateRepository.findByIdAndLawyerId(templateId, lawyerId)
                .orElseThrow(() -> {

                    if (!templateRepository.existsById(templateId)) {
                        return new NotFoundException("Template", templateId);
                    }
                    return new ForbiddenException("You do not have permission to modify this template");
                });
    }

    private Template getOwnedDraftTemplate(Long templateId, Long lawyerId) {
        final Template template = getOwnedTemplate(templateId, lawyerId);
        if (template.isPublished()) {
            throw new BusinessRuleException(
                    "Template id=" + templateId + " is already published", "TPL_001");
        }
        return template;
    }

    private List<TemplateField> buildFields(
            List<TemplateFieldRequest> fieldRequests,
            Template template,
            String createdBy
    ) {
        return fieldRequests.stream()
                .map(req -> TemplateField.builder()
                        .template(template)
                        .fieldKey(req.getFieldKey())
                        .label(req.getLabel())
                        .fieldType(req.getFieldType())
                        .required(req.isRequired())
                        .orderNum(req.getOrderNum())
                        .build()
                )
                .toList();
    }
}
