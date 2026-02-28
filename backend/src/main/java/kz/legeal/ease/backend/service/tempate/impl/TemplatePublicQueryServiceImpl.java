package kz.legeal.ease.backend.service.tempate.impl;


import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.dto.template.TemplatePreviewDto;
import kz.legeal.ease.backend.enums.TemplateStatus;
import kz.legeal.ease.backend.exception.template.TemplateNotFoundException;
import kz.legeal.ease.backend.mapper.TemplateMapper;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.service.tempate.TemplatePublicQueryService;
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
    public Page<TemplatePreviewDto> getPublishedTemplates(Category category, Pageable pageable) {
        return templateRepository.findAllPublished(TemplateStatus.PUBLISHED, category, pageable)
                .map(templateMapper::toPreviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateDto getPublishedTemplateById(Long templateId) {
        final var template = templateRepository.findByIdAndActive(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        if (!template.isPublished()) {
            throw new TemplateNotFoundException(templateId);
        }

        return templateMapper.toDto(template);
    }
}
