package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.MatchingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingRuleRepository extends JpaRepository<MatchingRule, Long> {

    List<MatchingRule> findAllByActiveTrue();

    List<MatchingRule> findAllByCategoryIdAndActiveTrue(Long categoryId);

    List<MatchingRule> findAllByTemplateIdAndActiveTrue(Long templateId);
}