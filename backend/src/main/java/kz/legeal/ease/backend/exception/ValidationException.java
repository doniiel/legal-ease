package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a value fails a business-level validation rule — distinct from
 * Jakarta Bean Validation ({@code @Valid}) which is handled by {@link GlobalExceptionHandler}.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Verification code is incorrect or expired</li>
 *   <li>A field value violates a custom domain constraint</li>
 * </ul>
 *
 * <p>Maps to HTTP 400 Bad Request by default. Supply an explicit {@link HttpStatus}
 * for cases like 429 Too Many Requests.</p>
 */
public class ValidationException extends BaseException {

    public ValidationException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public ValidationException(String message, String errorCode, HttpStatus status) {
        super(message, status, errorCode);
    }
}
