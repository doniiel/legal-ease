package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {

    List<RiskRule> findAllByTemplateIdAndActiveTrue(Long templateId);
}
