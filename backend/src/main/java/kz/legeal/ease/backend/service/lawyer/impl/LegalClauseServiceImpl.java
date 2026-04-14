package kz.legeal.ease.backend.service.lawyer.impl;

import kz.legeal.ease.backend.domain.Category;
import kz.legeal.ease.backend.domain.LegalClause;
import kz.legeal.ease.backend.dto.LegalClauseDto;
import kz.legeal.ease.backend.enums.AuditAction;
import kz.legeal.ease.backend.exception.BusinessRuleException;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.LegalClauseMapper;
import kz.legeal.ease.backend.repository.CategoryRepository;
import kz.legeal.ease.backend.repository.LegalClauseRepository;
import kz.legeal.ease.backend.request.LegalClauseRequest;
import kz.legeal.ease.backend.service.audit.AuditService;
import kz.legeal.ease.backend.service.lawyer.LegalClauseService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalClauseServiceImpl implements LegalClauseService {

    private final LegalClauseRepository clauseRepository;
    private final CategoryRepository    categoryRepository;
    private final LegalClauseMapper     clauseMapper;
    private final AuditService          auditService;

    @Override
    @Transactional
    public LegalClauseDto create(LegalClauseRequest request) {
        final var lawyer = currentLawyer();
        final var clause = LegalClause.builder()
                .lawyer(lawyer)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .category(resolveCategory(request.getCategoryId()))
                .active(true)
                .build();
        final var saved = clauseRepository.save(clause);
        auditService.log(lawyer.getId(), AuditAction.CLAUSE_CREATED, "LegalClause", saved.getId(), null);
        return clauseMapper.toDto(saved);
    }

    @Override
    @Transactional
    public LegalClauseDto update(Long id, LegalClauseRequest request) {
        final var lawyer = currentLawyer();
        final var clause = findOwned(id, lawyer.getId());
        clause.setTitle(request.getTitle());
        clause.setContent(request.getContent());
        clause.setTags(request.getTags());
        clause.setCategory(resolveCategory(request.getCategoryId()));
        final var saved = clauseRepository.save(clause);
        auditService.log(lawyer.getId(), AuditAction.CLAUSE_UPDATED, "LegalClause", saved.getId(), null);
        return clauseMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final var lawyer = currentLawyer();
        final var clause = findOwned(id, lawyer.getId());
        clause.setActive(false);
        clauseRepository.save(clause);
        auditService.log(lawyer.getId(), AuditAction.CLAUSE_DELETED, "LegalClause", id, null);
    }

    @Override
    @Transactional(readOnly = true)
    public LegalClauseDto getById(Long id) {
        final var lawyerId = currentLawyer().getId();
        return clauseMapper.toDto(findOwned(id, lawyerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LegalClauseDto> getMyActiveClauses(Pageable pageable) {
        final var lawyerId = currentLawyer().getId();
        return clauseRepository.findAllByLawyerIdAndActive(lawyerId, true, pageable)
                .map(clauseMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LegalClauseDto> search(Long categoryId, String keyword, Pageable pageable) {
        final var lawyerId = currentLawyer().getId();
        final String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        return clauseRepository.search(lawyerId, categoryId, kw, pageable)
                .map(clauseMapper::toDto);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private kz.legeal.ease.backend.domain.User currentLawyer() {
        final var user = SecurityUtils.requireCurrentUser();
        SecurityUtils.requireRole(user, "LAWYER");
        return user;
    }

    private LegalClause findOwned(Long id, Long lawyerId) {
        return clauseRepository.findByIdAndLawyerIdAndActive(id, lawyerId, true)
                .orElseThrow(() -> new NotFoundException("LegalClause", id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", categoryId));
    }
}
