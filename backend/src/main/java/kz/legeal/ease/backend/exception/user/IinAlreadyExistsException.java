package kz.legeal.ease.backend.exception.user;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class IinAlreadyExistsException extends BaseException {
    public IinAlreadyExistsException(String iin) {
        super("IIN '" + iin + "' is already registered", HttpStatus.CONFLICT, "USER_003");
    }
}