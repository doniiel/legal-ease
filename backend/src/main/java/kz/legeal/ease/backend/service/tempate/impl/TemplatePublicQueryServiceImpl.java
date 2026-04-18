package kz.legeal.ease.backend.service.tempate.impl;


import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.dto.template.TemplatePreviewDto;
import kz.legeal.ease.backend.enums.TemplateStatus;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.TemplateMapper;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.service.tempate.TemplatePublicQueryService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplatePublicQueryServiceImpl implements TemplatePublicQueryService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TemplatePreviewDto> getPublishedTemplates(Long categoryId, Pageable pageable) {
        SecurityUtils.requireAnyRole(SecurityUtils.requireCurrentUser(), "USER", "ADMIN");
        return templateRepository.findAllPublished(TemplateStatus.PUBLISHED, categoryId, pageable)
                .map(templateMapper::toPreviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateDto getPublishedTemplateById(Long templateId) {
        SecurityUtils.requireAnyRole(SecurityUtils.requireCurrentUser(), "USER", "ADMIN");
        final var template = templateRepository.findByIdAndActive(templateId)
                .orElseThrow(() -> new NotFoundException("Template", templateId));

        if (!template.isPublished()) {
            throw new NotFoundException("Template", templateId);
        }

        return templateMapper.toDto(template);
    }
}
