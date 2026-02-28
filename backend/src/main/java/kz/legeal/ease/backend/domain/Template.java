package kz.legeal.ease.backend.domain;


import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.TemplateStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Template extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "template_seq_gen",
            sequenceName = "template_seq_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "template_seq_gen"
    )
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TemplateStatus status = TemplateStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private User lawyer;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateField> templateFields = new ArrayList<>();

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = false;

    public boolean isOwnedBy(Long userId) {
        return this.lawyer != null && this.lawyer.getId().equals(userId);
    }

    public boolean isDraft() {
        return TemplateStatus.DRAFT.equals(this.status);
    }

    public boolean isPublished() {
        return TemplateStatus.PUBLISHED.equals(this.status);
    }

    public void publish() {
        this.status = TemplateStatus.PUBLISHED;
    }
}
