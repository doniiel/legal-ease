package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(String entity, String field, Object value) {
        super(entity + " with " + field + " '" + value + "' already exists",
                HttpStatus.CONFLICT, "ALREADY_EXISTS");
    }

    public AlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "ALREADY_EXISTS");
    }
}