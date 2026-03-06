package kz.legeal.ease.backend.exception.category;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends BaseException {
    public CategoryAlreadyExistsException(String name) {
        super("Category with name '" + name + "' already exists", HttpStatus.CONFLICT, "CATEGORY_003");
    }
}
