package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String entity, Object id) {
        super(entity + " not found with id: " + id,
                HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public NotFoundException(String entity, String field, Object value) {
        super(entity + " not found with " + field + ": " + value,
                HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
