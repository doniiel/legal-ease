package kz.legeal.ease.backend.service.tempate;


import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.request.TemplateRequest;

/**
 * Command for manage template
 * Permission only for Lawyer
 * Only write command
 **/
public interface TemplateCommandService {

    TemplateDto create(TemplateRequest request);

    TemplateDto update(Long templateId, TemplateRequest request);

    TemplateDto publish(Long templateId);

    void delete(Long templateId);
}
