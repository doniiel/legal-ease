package kz.legeal.ease.backend.exception;

import org.springframework.http.HttpStatus;

public class AccountNotActivatedException extends BaseException{

    public AccountNotActivatedException() {
        super("Account not activated", HttpStatus.FORBIDDEN, "NOT_CONFIRMED");
    }
}