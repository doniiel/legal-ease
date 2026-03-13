package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long> {

    List<ValidationRule> findAllByTemplateIdAndActiveTrue(Long templateId);
}
