package kz.legeal.ease.backend.service.tempate;

import kz.legeal.ease.backend.dto.template.TemplateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Read lawyer templates
 * Lawyer can see only own Templates include [DRAFT]
 * Only read command by owner
 **/
public interface TemplateQueryService {

    Page<TemplateDto> getMyTemplates(Pageable pageable);

    TemplateDto getMyTemplateById(Long templateId);
}
