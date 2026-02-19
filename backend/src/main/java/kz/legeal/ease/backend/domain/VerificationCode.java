package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.VerificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verification_code")
public class VerificationCode extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "verification_code_seq_gen",
            sequenceName = "verification_code_seq_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "verification_code_seq_gen"
    )
    private Long id;

    private String email;

    private String code;

    private VerificationType type;

    private boolean used = false;

    private LocalDateTime expiredAt;

    public boolean isExpired() {
        return this.expiredAt.isBefore(LocalDateTime.now());
    }
}
