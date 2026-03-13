package kz.legeal.ease.backend.repository;


import kz.legeal.ease.backend.domain.Document;
import kz.legeal.ease.backend.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.deleted = false")
    Page<Document> findAllByUserIdAndNotDeleted(Long userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.user.id = :userId AND d.deleted = false")
    Optional<Document> findByIdAndUserIdAndNotDeleted(Long id, Long userId);

    boolean existsByTemplateIdAndUserIdAndStatus(Long templateId, Long userId, DocumentStatus status);
}
