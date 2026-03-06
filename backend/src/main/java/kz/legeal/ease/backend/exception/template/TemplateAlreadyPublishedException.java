package kz.legeal.ease.backend.exception.template;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TemplateAlreadyPublishedException extends BaseException {
    public TemplateAlreadyPublishedException(Long templateId) {
        super("Template with id " + templateId + " is already published", HttpStatus.BAD_REQUEST, "TEMPLATE_003");
    }
}
