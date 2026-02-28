package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.request.CategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CategoryRequest request);

    CategoryDto update(Long id, CategoryRequest request);

    void delete(Long id);

    Page<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long id);

    List<CategoryDto> getActiveCategories();
}
