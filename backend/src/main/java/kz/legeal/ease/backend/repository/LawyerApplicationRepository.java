package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LawyerApplicationRepository extends JpaRepository<LawyerApplication, Long>, JpaSpecificationExecutor<LawyerApplication> {

    Optional<LawyerApplication> findById(Long id);

    boolean existsByUserAndStatusIn(User user, Collection<Status> statuses);

    Optional<LawyerApplication> findTopByUserOrderByCreatedDateDesc(User user);

    Optional<LawyerApplication> findTopByUserAndStatusInOrderByCreatedDateDesc(User user, List<Status> statuses);
}
