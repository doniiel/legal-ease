package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
public class Role extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(name = "role_seq_gen", sequenceName = "role_seq_gen", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq_gen")
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}