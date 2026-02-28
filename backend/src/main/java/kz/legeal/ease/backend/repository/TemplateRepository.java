package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.Template;
import kz.legeal.ease.backend.enums.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    @Query("""
            SELECT t FROM Template t
            LEFT JOIN FETCH t.templateFields tf
            WHERE t.lawyer.id = :lawyerId
              AND t.active = false
            ORDER BY t.createdDate DESC
            """)
    Page<Template> findAllByLawyerId(
            @Param("lawyerId") Long lawyerId,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Template t
            WHERE t.status = :status
              AND t.active = false
              AND (:category IS NULL OR t.category = :category)
            ORDER BY t.createdDate DESC
            """)
    Page<Template> findAllPublished(
            @Param("status") TemplateStatus status,
            @Param("category") Category category,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Template t
            LEFT JOIN FETCH t.templateFields tf
            LEFT JOIN FETCH t.lawyer l
            WHERE t.id = :id AND t.active = true
            """)
    Optional<Template> findByIdAndActive(@Param("id") Long id);

    @Query("""
            SELECT t FROM Template t
            WHERE t.id = :id
              AND t.lawyer.id = :lawyerId
              AND t.active = false
            """)
    Optional<Template> findByIdAndLawyerId(
            @Param("id") Long id,
            @Param("lawyerId") Long lawyerId
    );
}