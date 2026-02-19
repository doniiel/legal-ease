package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.VerificationCode;
import kz.legeal.ease.backend.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findTopByEmailAndTypeAndUsedFalseOrderByCreatedDateDesc(String email, VerificationType type);
}
