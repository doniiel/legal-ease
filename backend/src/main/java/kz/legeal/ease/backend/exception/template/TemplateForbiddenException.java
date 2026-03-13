package kz.legeal.ease.backend.exception.template;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TemplateForbiddenException extends BaseException {
    public TemplateForbiddenException() {
        super("You do not have permission to modify this template", HttpStatus.FORBIDDEN, "TEMPLATE_002");
    }
}
