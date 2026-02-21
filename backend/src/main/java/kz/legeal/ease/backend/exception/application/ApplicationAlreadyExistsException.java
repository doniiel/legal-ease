package kz.legeal.ease.backend.exception.application;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ApplicationAlreadyExistsException extends BaseException {
    public ApplicationAlreadyExistsException() {
        super("You already have an active or approved application", HttpStatus.CONFLICT, "APP_002");
    }
}
