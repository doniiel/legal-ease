package kz.legeal.ease.backend.exception;

public class BaseException extends RuntimeException {

    private int status;

    public BaseException(String message) {
        super(message);
    }
}
