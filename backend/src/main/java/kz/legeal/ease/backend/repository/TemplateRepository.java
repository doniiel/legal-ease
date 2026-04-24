package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.enums.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    @Query("""
            SELECT t FROM Template t
            WHERE t.lawyer.id = :lawyerId
              AND t.active = true
            ORDER BY t.createdDate DESC
            """)
    Page<Template> findAllByLawyerId(@Param("lawyerId") Long lawyerId, Pageable pageable);

    @Query("""
            SELECT t FROM Template t
            WHERE t.status = :status
              AND t.active = true
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
            """)
    Page<Template> findAllPublished(
            @Param("status") TemplateStatus status,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT t FROM Template t WHERE t.id = :id AND t.active = true")
    Optional<Template> findByIdAndActive(@Param("id") Long id);

    @Query("""
            SELECT t FROM Template t
            WHERE t.id = :id
              AND t.lawyer.id = :lawyerId
              AND t.active = true
            """)
    Optional<Template> findByIdAndLawyerId(@Param("id") Long id, @Param("lawyerId") Long lawyerId);

    @Query("""
            SELECT t FROM Template t
            WHERE t.status = kz.legeal.ease.backend.enums.TemplateStatus.PUBLISHED
              AND t.active = true
              AND LOWER(t.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY t.createdDate DESC
            """)
    List<Template> findPublishedByCategoryKeyword(@Param("keyword") String keyword);
}