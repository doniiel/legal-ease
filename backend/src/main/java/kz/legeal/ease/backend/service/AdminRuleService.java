package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.dto.*;

import java.util.List;

/**
 * Admin-level rule management.
 *
 * <p>Admins cannot create or edit rule content — that is the lawyer's domain.
 * Admins can:
 * <ul>
 *   <li>View all rules across all templates (for audit and oversight).</li>
 *   <li>Toggle any rule active/inactive to control which rules are applied globally.</li>
 * </ul>
 *
 * <p>Every toggle operation is automatically audited via {@code AbstractAuditingEntity}
 * ({@code updated_by} and {@code updated_date} are set to the admin's email and current time).
 */
public interface AdminRuleService {

    // ── Validation Rules ──────────────────────────────────────────────────────

    List<ValidationRuleDto> getAllValidationRules();

    ValidationRuleDto toggleValidationRule(Long id);

    // ── Risk Rules ────────────────────────────────────────────────────────────

    List<RiskRuleDto> getAllRiskRules();

    RiskRuleDto toggleRiskRule(Long id);

    // ── Matching Rules ────────────────────────────────────────────────────────

    List<MatchingRuleDto> getAllMatchingRules();

    MatchingRuleDto toggleMatchingRule(Long id);

    // ── Conditional Rules ─────────────────────────────────────────────────────

    List<ConditionalRuleDto> getAllConditionalRules();

    ConditionalRuleDto toggleConditionalRule(Long id);

    // ── Required Document Rules ───────────────────────────────────────────────

    List<RequiredDocRuleDto> getAllRequiredDocRules();

    RequiredDocRuleDto toggleRequiredDocRule(Long id);
}
