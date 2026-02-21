package kz.legeal.ease.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private final String requestId;
    private final int status;
    private final String errorCode;      // "USER_001", "AUTH_003" и тд
    private final String message;
    private final List<FieldError> errors; // для validation ошибок
    private final LocalDateTime timestamp;
    private final String path;

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
    }
}