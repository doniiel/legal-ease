package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.RequiredDocRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequiredDocRuleRepository extends JpaRepository<RequiredDocRule, Long> {

    /** Used by RequiredDocsHandler at rule-engine evaluation time. */
    List<RequiredDocRule> findAllByTemplateIdAndActiveTrue(Long templateId);

    /** Used by admin to list all rules (including inactive). */
    List<RequiredDocRule> findAllByTemplateId(Long templateId);
}
