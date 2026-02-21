package kz.legeal.ease.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import kz.legeal.ease.backend.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDto> handleBaseException(
            BaseException ex, HttpServletRequest request) {

        log.warn("[{}] {} - {}", ex.getErrorCode(), request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(ex.getStatus())
                .body(buildError(request, ex.getStatus().value(), ex.getErrorCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        final var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorResponseDto.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        log.warn("[VALIDATION_001] {} - {} field errors", request.getRequestURI(), fieldErrors.size());

        return ResponseEntity.badRequest()
                .body(buildError(request, 400, "VALIDATION_001", "Validation failed", fieldErrors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("[ACCESS_DENIED] {} - {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(request, 403, "ACCESS_001", "Access denied", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("[INTERNAL_ERROR] {} - ", request.getRequestURI(), ex);

        return ResponseEntity.internalServerError()
                .body(buildError(request, 500, "INTERNAL_001", "Internal server error", null));
    }

    private ErrorResponseDto buildError(
            HttpServletRequest request,
            int status,
            String errorCode,
            String message,
            java.util.List<ErrorResponseDto.FieldError> errors) {

        return ErrorResponseDto.builder()
                .requestId(resolveRequestId(request))
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }

    private String resolveRequestId(HttpServletRequest request) {
        final var existing = request.getHeader("X-Request-ID");
        return (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
    }
}