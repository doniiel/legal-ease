package kz.legeal.ease.backend.exception.application;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ApplicationCannotBeDeletedException extends BaseException {
  public ApplicationCannotBeDeletedException(Long id) {
    super("Approved application with id '" + id + "' cannot be deleted", HttpStatus.CONFLICT, "APP_004");
  }
}
