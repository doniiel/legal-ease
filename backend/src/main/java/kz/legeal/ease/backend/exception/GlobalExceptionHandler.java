package kz.legeal.ease.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Central exception handler for the entire API surface.
 *
 * <p>Maps every exception type to an {@link ErrorResponseDto} with a consistent shape
 * so clients always receive the same JSON structure regardless of the error type.
 *
 * <p>Handler priority (most specific first):
 * <ol>
 *   <li>{@link StorageException} → 503 Service Unavailable</li>
 *   <li>{@link BaseException} subclasses (NotFoundException, ValidationException, …) → dynamic status</li>
 *   <li>Jakarta {@link MethodArgumentNotValidException} → 400 with per-field details</li>
 *   <li>Spring Security {@link AccessDeniedException} → 403</li>
 *   <li>{@link IllegalStateException} → 409 Conflict</li>
 *   <li>Catch-all {@link Exception} → 500</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── S3 / MinIO ────────────────────────────────────────────────────────────

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponseDto> handleStorage(
            StorageException ex, HttpServletRequest request) {

        log.error("[{}] {} — {}", ex.getCode(), request.getRequestURI(), ex.getMessage(), ex);
        return build(request, HttpStatus.SERVICE_UNAVAILABLE, ex.getCode(), ex.getMessage(), null);
    }

    // ── Domain / Business exceptions ──────────────────────────────────────────

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDto> handleBase(
            BaseException ex, HttpServletRequest request) {

        log.warn("[{}] {} — {}", ex.getErrorCode(), request.getRequestURI(), ex.getMessage());
        return build(request, ex.getStatus(), ex.getErrorCode(), ex.getMessage(), null);
    }

    // ── Jakarta Bean Validation ───────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        final var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorResponseDto.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        log.warn("[VALIDATION_001] {} — {} field error(s)", request.getRequestURI(), fieldErrors.size());
        return build(request, HttpStatus.BAD_REQUEST, "VALIDATION_001", "Validation failed", fieldErrors);
    }

    // ── Spring Security ───────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("[ACCESS_DENIED] {} — {}", request.getRequestURI(), ex.getMessage());
        return build(request, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", null);
    }

    // ── Java runtime ──────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {

        log.warn("[CONFLICT] {} — {}", request.getRequestURI(), ex.getMessage());
        return build(request, HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), null);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("[INTERNAL_ERROR] {} — ", request.getRequestURI(), ex);
        return build(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.", null);
    }

    // ── Builder helper ────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponseDto> build(
            HttpServletRequest request,
            HttpStatus status,
            String errorCode,
            String message,
            List<ErrorResponseDto.FieldError> fieldErrors) {

        final var body = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .requestId(resolveRequestId(request))
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private String resolveRequestId(HttpServletRequest request) {
        final var existing = request.getHeader("X-Request-ID");
        return (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
    }
}
