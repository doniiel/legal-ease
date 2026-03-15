package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.LegalClause;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LegalClauseRepository extends JpaRepository<LegalClause, Long> {

    Page<LegalClause> findAllByLawyerIdAndActive(Long lawyerId, boolean active, Pageable pageable);

    Optional<LegalClause> findByIdAndLawyerIdAndActive(Long id, Long lawyerId, boolean active);

    @Query("""
            SELECT c FROM LegalClause c
            WHERE c.lawyer.id = :lawyerId AND c.active = true
              AND (:categoryId IS NULL OR c.category.id = :categoryId)
              AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<LegalClause> search(Long lawyerId, Long categoryId, String keyword, Pageable pageable);
}
