package kz.legeal.ease.backend.exception.application;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ApplicationAlreadyProcessedException extends BaseException {
    public ApplicationAlreadyProcessedException(Long id) {
        super("Application with id '" + id + "' has already been processed", HttpStatus.CONFLICT, "APP_003");
    }
}
