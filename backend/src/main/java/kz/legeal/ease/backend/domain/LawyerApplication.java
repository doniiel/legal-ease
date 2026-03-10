package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.Status;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lawyer")
public class LawyerApplication extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "lawyer_app_seq",
            sequenceName = "lawyer_app_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "lawyer_app_seq"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "license_number", nullable = false, length = 50, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Version
    private Long version;

    public boolean isPending() {
        return this.status == Status.PENDING;
    }

    public boolean isApproved() {
        return this.status == Status.APPROVED;
    }

}
