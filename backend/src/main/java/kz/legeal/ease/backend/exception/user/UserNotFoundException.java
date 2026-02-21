package kz.legeal.ease.backend.exception.user;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String email) {
        super("User with email '" + email + "' not found", HttpStatus.NOT_FOUND, "USER_001");
    }
    public UserNotFoundException(Long id) {
        super("User with id '" + id + "' not found", HttpStatus.NOT_FOUND, "USER_001");
    }
}