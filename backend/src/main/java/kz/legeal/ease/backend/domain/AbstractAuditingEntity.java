package kz.legeal.ease.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class AbstractAuditingEntity {

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    public AbstractAuditingEntity() {
        this.createdBy = "system";
        this.createdDate = LocalDateTime.now();
    }
}
