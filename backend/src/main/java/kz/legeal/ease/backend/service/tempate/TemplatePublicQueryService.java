package kz.legeal.ease.backend.service.tempate;

import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.dto.template.TemplatePreviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Public read templates for User
 * Fetch only Published templates
 * Public read permission
 **/
public interface TemplatePublicQueryService {

    Page<TemplatePreviewDto> getPublishedTemplates(Long categoryId, Pageable pageable);

    TemplateDto getPublishedTemplateById(Long templateId);
}
