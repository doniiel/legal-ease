package kz.legeal.ease.backend.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingRuleRequest {

    private Long templateId;

    private Long categoryId;

    private String keywords;

    private Integer baseScore;
}
