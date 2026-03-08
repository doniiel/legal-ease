package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.ConditionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionalRuleRepository extends JpaRepository<ConditionRule, Long> {

    List<ConditionRule> findAllByTemplateIdAndActiveTrue(Long templateId);
}
