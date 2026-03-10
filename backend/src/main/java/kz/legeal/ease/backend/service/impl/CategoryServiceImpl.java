package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.domain.Category;
import kz.legeal.ease.backend.dto.CategoryDto;
import kz.legeal.ease.backend.exception.category.CategoryAlreadyExistsException;
import kz.legeal.ease.backend.exception.category.CategoryHasTemplatesException;
import kz.legeal.ease.backend.exception.category.CategoryNotFoundException;
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
        final var currentAdmin = SecurityUtils.getCurrentUserOrThrow();

        log.info(
                "Admin [{}] attempting to create category with name='{}'",
                currentAdmin.getEmail(),
                request.getName()
        );

        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            log.warn(
                    "Category creation failed: category with name='{}' already exists. RequestedBy={}",
                    request.getName(),
                    currentAdmin.getEmail()
            );
            throw new CategoryAlreadyExistsException(request.getName());
        }

        final var category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        final var savedCategory = categoryRepository.save(category);

        log.info(
                "Category created successfully: id={}, name='{}', createdBy={}",
                savedCategory.getId(),
                savedCategory.getName(),
                currentAdmin.getEmail()
        );
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, UpdateCategoryRequest request) {
        final var currentAdmin = SecurityUtils.getCurrentUserOrThrow();
        log.info(
                "Admin [{}] attempting to update category id={}",
                currentAdmin.getEmail(),
                id
        );

        final var category = findByIdOrThrow(id);

        if (!category.getName().equalsIgnoreCase(request.getName().trim())
                && categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            log.warn(
                    "Category update failed: name '{}' already exists. categoryId={}, requestedBy={}",
                    request.getName(),
                    id,
                    currentAdmin.getEmail()
            );
            throw new CategoryAlreadyExistsException(request.getName());
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());

        final var updated = categoryRepository.save(category);

        log.info(
                "Category updated successfully: id={}, newName='{}', updatedBy={}",
                updated.getId(),
                updated.getName(),
                currentAdmin.getEmail()
        );
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var currentAdmin = SecurityUtils.getCurrentUserOrThrow();
        log.info(
                "Admin [{}] attempting to deactivate category id={}",
                currentAdmin.getEmail(),
                id
        );

        final var category = findByIdOrThrow(id);

        if (categoryRepository.hasActiveTemplates(id)) {
            log.warn(
                    "Category deactivation blocked: category id={} has active templates. RequestedBy={}",
                    id,
                    currentAdmin.getEmail()
            );
            throw new CategoryHasTemplatesException(id);
        }

        category.deactivate();
        categoryRepository.save(category);

        log.info(
                "Category deactivated successfully: id={}, deactivatedBy={}",
                id,
                currentAdmin.getEmail()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAll(Pageable pageable) {
        log.debug(
                "Fetching categories page: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        return categoryRepository.findAllSorted(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        log.debug("Fetching category by id={}", id);
        return categoryMapper.toDto(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        log.debug("Fetching active categories ordered by name");
        return categoryMapper.toDtoList(
                categoryRepository.findAllByActiveTrueOrderByNameAsc()
        );
    }

    private Category findByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
