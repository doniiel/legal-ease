package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.Category;
import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.mapper.CategoryMapper;
import kz.legeal.ease.backend.repository.CategoryRepository;
import kz.legeal.ease.backend.request.CategoryRequest;
import kz.legeal.ease.backend.service.CategoryService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto create(CategoryRequest request) {
        final var curentAdmin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException(request.getName());
        }

        final var category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        log.info("Admin {} created category: {}", curentAdmin.getEmail(), request.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryRequest request) {
        final var curentAdmin = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        final var category = findByIdOrThrow(id);

        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException(request.getName());
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());

        log.info("Admin {} updated category id={}", curentAdmin.getEmail(), id);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var category = findByIdOrThrow(id);

        // Запрещено удалять если есть привязанные шаблоны
        if (categoryRepository.hasActiveTemplates(id)) {
            throw new CategoryHasTemplatesException(id);
        }

        // Soft delete через деактивацию
        category.deactivate();
        categoryRepository.save(category);

        log.info("Admin {} deactivated category id={}", securityUtils.getCurrentUserEmail(), id);
    }

    @Override
    @Transactional
    public Page<CategoryDto> getAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public CategoryDto getById(Long id) {
        return null;
    }

    @Override
    @Transactional
    public List<CategoryDto> getActiveCategories() {
        return List.of();
    }
}
