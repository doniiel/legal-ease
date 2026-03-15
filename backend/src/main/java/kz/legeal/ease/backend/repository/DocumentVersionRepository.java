package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findAllByDocumentIdOrderByVersionAsc(Long documentId);

    Optional<DocumentVersion> findByDocumentIdAndVersion(Long documentId, int version);
}
