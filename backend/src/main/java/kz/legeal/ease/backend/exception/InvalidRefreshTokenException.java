package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends BaseException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or has been revoked", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
    }

    public InvalidRefreshTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
    }
}
