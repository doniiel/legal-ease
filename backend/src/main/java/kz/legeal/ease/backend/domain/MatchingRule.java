package kz.legeal.ease.backend.domain;

import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "matching_rules")
public class MatchingRule extends AbstractAuditingEntity {


}
