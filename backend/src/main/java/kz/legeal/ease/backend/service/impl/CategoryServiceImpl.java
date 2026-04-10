package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.Category;
import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.CategoryMapper;
import kz.legeal.ease.backend.repository.CategoryRepository;
import kz.legeal.ease.backend.request.category.CreateCategoryRequest;
import kz.legeal.ease.backend.request.category.UpdateCategoryRequest;
import kz.legeal.ease.backend.service.CategoryService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto create(CreateCategoryRequest request) {
        final var curentAdmin = SecurityUtils.requireCurrentUser();
        SecurityUtils.requireRole(curentAdmin, "ADMIN");

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessRuleException(
                    "Category '" + request.getName() + "' already exists", "CAT_001", HttpStatus.CONFLICT);
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
    public CategoryDto update(Long id, UpdateCategoryRequest request) {
        final var curentAdmin = SecurityUtils.requireCurrentUser();
        SecurityUtils.requireRole(curentAdmin, "ADMIN");
        final var category = findByIdOrThrow(id);

        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessRuleException(
                    "Category '" + request.getName() + "' already exists", "CAT_001", HttpStatus.CONFLICT);
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());

        log.info("Admin {} updated category id={}", curentAdmin.getEmail(), id);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var currentAdmin = SecurityUtils.requireCurrentUser();
        SecurityUtils.requireRole(currentAdmin, "ADMIN");
        final var category = findByIdOrThrow(id);

        if (categoryRepository.hasActiveTemplates(id)) {
            throw new BusinessRuleException(
                    "Cannot delete category id=" + id + " — it still has active templates", "CAT_002");
        }

        category.deactivate();
        categoryRepository.save(category);

        log.info("Admin {} deactivated category id={}", currentAdmin.getEmail(), id);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAll(Pageable pageable) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return categoryRepository.findAllSorted(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return categoryMapper.toDto(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        return categoryMapper.toDtoList(
                categoryRepository.findAllByActiveTrueOrderByNameAsc()
        );
    }

    private Category findByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }
}
