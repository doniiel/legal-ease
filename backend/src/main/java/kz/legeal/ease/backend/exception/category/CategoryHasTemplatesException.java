package kz.legeal.ease.backend.exception.category;

import kz.legeal.ease.backend.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CategoryHasTemplatesException extends BaseException {
    public CategoryHasTemplatesException(Long id) {
        super("Cannot delete category with id " + id + ": it has active templates", HttpStatus.BAD_REQUEST, "CATEGORY_001");
    }
}
