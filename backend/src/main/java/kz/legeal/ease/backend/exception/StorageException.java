package kz.legeal.ease.backend.exception;

/**
 * Thrown when an S3/MinIO storage operation fails in a way that the caller should surface to the user.
 * Maps to HTTP 503 via the global exception handler.
 */
public class StorageException extends RuntimeException {

    private final String code;

    public StorageException(String message, String code) {
        super(message);
        this.code = code;
    }

    public StorageException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
