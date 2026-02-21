package kz.legeal.ease.backend.exception.auth;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class AccountLockedException extends BaseException {
    public AccountLockedException(LocalDateTime lockedUntil) {
        super("Account is locked until " + lockedUntil, HttpStatus.FORBIDDEN, "AUTH_003");
    }
}