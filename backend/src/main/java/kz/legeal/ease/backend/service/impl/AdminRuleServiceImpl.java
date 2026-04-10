package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.dto.*;
import kz.legeal.ease.backend.exception.NotFoundException;
import kz.legeal.ease.backend.mapper.*;
import kz.legeal.ease.backend.repository.*;
import kz.legeal.ease.backend.service.AdminRuleService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRuleServiceImpl implements AdminRuleService {

    private final ValidationRuleRepository  validationRuleRepository;
    private final RiskRuleRepository        riskRuleRepository;
    private final MatchingRuleRepository    matchingRuleRepository;
    private final ConditionalRuleRepository conditionalRuleRepository;
    private final RequiredDocRuleRepository requiredDocRuleRepository;

    private final ValidationMapper       validationMapper;
    private final RiskRuleMapper         riskRuleMapper;
    private final MatchingRuleMapper     matchingRuleMapper;
    private final ConditionalRuleMapper  conditionalRuleMapper;
    private final RequiredDocRuleMapper  requiredDocRuleMapper;

    // ── Validation Rules ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ValidationRuleDto> getAllValidationRules() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return validationRuleRepository.findAll().stream()
                .map(validationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ValidationRuleDto toggleValidationRule(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        final var rule = validationRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ValidationRule", id));
        rule.setActive(!rule.isActive());
        return validationMapper.toDto(validationRuleRepository.save(rule));
    }

    // ── Risk Rules ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RiskRuleDto> getAllRiskRules() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return riskRuleRepository.findAll().stream()
                .map(riskRuleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RiskRuleDto toggleRiskRule(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        final var rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RiskRule", id));
        rule.setActive(!rule.isActive());
        return riskRuleMapper.toDto(riskRuleRepository.save(rule));
    }

    // ── Matching Rules ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MatchingRuleDto> getAllMatchingRules() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return matchingRuleRepository.findAll().stream()
                .map(matchingRuleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MatchingRuleDto toggleMatchingRule(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        final var rule = matchingRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("MatchingRule", id));
        rule.setActive(!rule.isActive());
        return matchingRuleMapper.toDto(matchingRuleRepository.save(rule));
    }

    // ── Conditional Rules ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ConditionalRuleDto> getAllConditionalRules() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return conditionalRuleRepository.findAll().stream()
                .map(conditionalRuleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ConditionalRuleDto toggleConditionalRule(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        final var rule = conditionalRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ConditionalRule", id));
        rule.setActive(!rule.isActive());
        return conditionalRuleMapper.toDto(conditionalRuleRepository.save(rule));
    }

    // ── Required Document Rules ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RequiredDocRuleDto> getAllRequiredDocRules() {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        return requiredDocRuleRepository.findAll().stream()
                .map(requiredDocRuleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RequiredDocRuleDto toggleRequiredDocRule(Long id) {
        SecurityUtils.requireRole(SecurityUtils.requireCurrentUser(), "ADMIN");
        final var rule = requiredDocRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequiredDocRule", id));
        rule.setActive(!rule.isActive());
        return requiredDocRuleMapper.toDto(requiredDocRuleRepository.save(rule));
    }
}
