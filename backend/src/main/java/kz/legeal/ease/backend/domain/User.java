package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.Gender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_iin",   columnList = "iin")
})
public class User extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq_gen", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    private Long id;

    @Column(name = "iin", unique = true, nullable = false, length = 12)
    private String iin;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone", unique = true, nullable = false, length = 20)
    private String phone;

    @Column(name = "fio", nullable = false, length = 255)
    private String fio;

    @Column(name = "password", nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "last_password_modified_date")
    private LocalDateTime lastPasswordModifiedDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    public boolean hasRole(String roleCode) {
        return userRoles.stream()
                .filter(UserRole::isActive)
                .anyMatch(ur -> ur.getRole().getCode().equals(roleCode));
    }
}