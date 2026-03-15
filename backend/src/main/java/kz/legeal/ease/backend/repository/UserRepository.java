package kz.legeal.ease.backend.repository;

import kz.legeal.ease.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
                SELECT u FROM User u
                LEFT JOIN FETCH u.userRoles ur
                LEFT JOIN FETCH ur.role
                WHERE u.email = :email
                  AND u.deleted = false
            """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
                SELECT u FROM User u
                LEFT JOIN FETCH u.userRoles ur
                LEFT JOIN FETCH ur.role
                WHERE u.id = :id
                  AND u.deleted = false
            """)
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    long countByDeletedFalse();

    @Query("""
            SELECT COUNT(DISTINCT ur.user.id) FROM UserRole ur
            WHERE ur.role.code = :roleCode
              AND ur.user.deleted = false
              AND ur.active = true
            """)
    long countActiveByRoleCode(@Param("roleCode") String roleCode);
}
