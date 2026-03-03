package kz.legeal.ease.backend.exception.template;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TemplateNotFoundException extends BaseException {
    public TemplateNotFoundException(Long templateId) {
        super("Template not found with id: " + templateId, HttpStatus.NOT_FOUND, "TEMPLATE_001");
    }
}
