package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateFieldRepository extends JpaRepository<TemplateField, Long> {
}