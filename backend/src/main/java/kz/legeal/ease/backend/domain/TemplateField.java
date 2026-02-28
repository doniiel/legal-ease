package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.TemplateFieldType;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "template_fields",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_template_field_key",
                columnNames = {"template_id", "field_key"}
        )
)
public class TemplateField extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "template_field_seq_gen",
            sequenceName = "template_field_seq_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "template_field_seq_gen"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "field_key", nullable = false, length = 100)
    private String fieldKey;

    @Column(name = "label", nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private TemplateFieldType fieldType;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "order_num", nullable = false)
    @Builder.Default
    private int orderNum = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
