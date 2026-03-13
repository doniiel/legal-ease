package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "matching_rules")
public class MatchingRule extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "matching_rule_seq"
    )
    @SequenceGenerator(
            name = "matching_rule_seq",
            sequenceName = "matching_rule_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "keywords", columnDefinition = "TEXT", nullable = false)
    private String keywords;

    @Column(name = "base_score", nullable = false)
    @Builder.Default
    private int baseScore = 50;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Разбивает CSV-строку ключевых слов в список.
     * "аренда, жильё, договор" → ["аренда", "жильё", "договор"]
     */
    public List<String> getKeywordList() {
        if (keywords == null || keywords.isBlank()) return List.of();
        return List.of(keywords.toLowerCase().split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}