package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class Role extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "role_seq_gen",
            sequenceName = "role_seq_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "role_seq_gen"
    )
    private Long id;

    private String code;

    @OneToMany(mappedBy = "role")
    private Set<UserRole> userRoles = new HashSet<>();

    private boolean active = true;

    private boolean blocked = false;
}
