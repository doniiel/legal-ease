package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails — invalid credentials, expired or revoked tokens,
 * or unactivated accounts attempting to authenticate.
 *
 * <p>Maps to HTTP 401 Unauthorized.</p>
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
