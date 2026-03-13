package kz.legeal.ease.backend.service.rule.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResult {

    private boolean approved;

    private String summary;

    private String recommendation;
}
