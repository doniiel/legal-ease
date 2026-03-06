package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "document_field_values")
public class DocumentFieldValue extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "document_field_value_seq_gen",
            sequenceName = "document_field_value_seq_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "document_field_value_seq_gen"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Document document;

    private String fieldKey;

    private String fieldValue;
}
