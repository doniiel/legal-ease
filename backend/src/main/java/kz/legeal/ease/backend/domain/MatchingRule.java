package kz.legeal.ease.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "matching_rules")
public class MatchingRule extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "matching_rules_seq"
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
    private int baseScore = 50;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public List<String> getKeywordList() {
        return List.of(keywords.toLowerCase().split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
