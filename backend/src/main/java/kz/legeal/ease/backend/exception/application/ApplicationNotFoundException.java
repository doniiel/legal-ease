package kz.legeal.ease.backend.exception.application;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ApplicationNotFoundException extends BaseException {
    public ApplicationNotFoundException(Long id) {
        super("Application with id '" + id + "' not found", HttpStatus.NOT_FOUND, "APP_001");
    }

    public ApplicationNotFoundException(String email) {
        super("Application for email '" + email + "' not found", HttpStatus.NOT_FOUND, "APP_001");
    }
}

