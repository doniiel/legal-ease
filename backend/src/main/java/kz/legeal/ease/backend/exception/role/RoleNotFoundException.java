package kz.legeal.ease.backend.exception.role;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends BaseException {
    public RoleNotFoundException(String code) {
        super("Role '" + code + "' not found", HttpStatus.NOT_FOUND, "ROLE_001");
    }
}
