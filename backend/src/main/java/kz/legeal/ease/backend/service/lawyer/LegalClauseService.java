package kz.legeal.ease.backend.service.lawyer;

import kz.legeal.ease.backend.dto.LegalClauseDto;
import kz.legeal.ease.backend.request.LegalClauseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Clause knowledge base management for the currently authenticated lawyer.
 */
public interface LegalClauseService {

    LegalClauseDto create(LegalClauseRequest request);

    LegalClauseDto update(Long id, LegalClauseRequest request);

    void delete(Long id);

    LegalClauseDto getById(Long id);

    /** Paginated list of all active clauses owned by the current lawyer. */
    Page<LegalClauseDto> getMyActiveClauses(Pageable pageable);

    /**
     * Search active clauses owned by the current lawyer.
     *
     * @param categoryId optional category filter
     * @param keyword    optional keyword matched against title and tags
     */
    Page<LegalClauseDto> search(Long categoryId, String keyword, Pageable pageable);
}
