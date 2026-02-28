package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    Page<Category> findAll(Pageable pageable);

    List<Category> findAllByActiveTrueOrderByNameAsc();

    @Query("SELECT COUNT(t) > 0 FROM Template t WHERE t.category.id = :categoryId AND t.active = false")
    boolean hasActiveTemplates(@Param("categoryId") Long categoryId);

}
