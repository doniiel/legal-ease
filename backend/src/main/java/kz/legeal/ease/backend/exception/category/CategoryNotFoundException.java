package kz.legeal.ease.backend.exception.category;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BaseException {
    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id, HttpStatus.NOT_FOUND, "CATEGORY_002");
    }
}
