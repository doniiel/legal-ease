package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import kz.legeal.ease.backend.enums.DocumentStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class Document extends AbstractAuditingEntity {

    @Id
    @SequenceGenerator(
            name = "document_seq_gen",
            sequenceName = "document_seq_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "document_seq_gen"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Template template;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "title")
    private String title;

    @Builder.Default
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentFieldValue> fieldValues = new ArrayList<>();

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    /**
     * S3 / MinIO object key of the most recent PDF version (e.g. {@code "documents/7/42/v2.pdf"}).
     * {@code null} for DRAFT documents or when PDF upload has not yet succeeded.
     */
    @Column(name = "s3_object_key", length = 512)
    private String s3ObjectKey;

    /** Monotonically increasing version counter. 0 = never completed. */
    @Column(name = "current_version", nullable = false)
    @Builder.Default
    private int currentVersion = 0;

}

