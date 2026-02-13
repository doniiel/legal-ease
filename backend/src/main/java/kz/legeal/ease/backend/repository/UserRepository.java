package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalseAndActiveTrue(String email);

    Optional<User> findByIdAndDeletedFalseAndActiveTrue(Long id);
}
