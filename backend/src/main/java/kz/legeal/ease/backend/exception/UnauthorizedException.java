package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
