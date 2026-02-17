package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.LawyerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LawyerApplicationRepository extends JpaRepository<LawyerApplication, Long> {

    Optional<LawyerApplication> findById(Long id);
}
