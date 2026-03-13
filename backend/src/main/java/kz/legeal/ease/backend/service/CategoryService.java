package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.request.category.CreateCategoryRequest;
import kz.legeal.ease.backend.request.category.UpdateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CreateCategoryRequest request);

    CategoryDto update(Long id, UpdateCategoryRequest request);

    void delete(Long id);

    Page<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long id);

    List<CategoryDto> getActiveCategories();
}
