package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule or domain constraint is violated.
 *
 * <p>Default mapping is HTTP 400 Bad Request. Pass an explicit {@link HttpStatus}
 * (e.g. {@code CONFLICT}) for semantically distinct cases like duplicate-resource errors.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Attempting to edit a COMPLETED document → 400</li>
 *   <li>Registering with an email that already exists → 409</li>
 *   <li>Approving an already-processed application → 400</li>
 * </ul>
 */
public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_ERROR");
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public BusinessRuleException(String message, String errorCode, HttpStatus status) {
        super(message, status, errorCode);
    }
}
