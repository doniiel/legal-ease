package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "document_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "version"}))
public class DocumentVersion extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(name = "document_version_seq", sequenceName = "document_version_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_version_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "s3_object_key", nullable = false, length = 512)
    private String s3ObjectKey;
}
