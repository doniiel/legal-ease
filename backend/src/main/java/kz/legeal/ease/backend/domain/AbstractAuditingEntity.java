package kz.legeal.ease.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAuditingEntity {

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        final var currentUser = resolveCurrentUser();
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.createdBy = currentUser;
        this.updatedBy = currentUser;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
        this.updatedBy = resolveCurrentUser();
    }

    private String resolveCurrentUser() {
        try {
            return SecurityUtils.getCurrentUser()
                    .map(User::getEmail)
                    .orElse("system");
        } catch (Exception e) {
            return "system";
        }
    }
}