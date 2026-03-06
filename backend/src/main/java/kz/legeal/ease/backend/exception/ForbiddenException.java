package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        super("Access denied to this resource", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}