package kz.legeal.ease.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingRuleDto {

    private Long id;

    private Long templateId;

    private String templateTitle;

    private Long categoryId;

    private String categoryName;

    private String keywords;

    private int baseScore;

    private boolean active;
}
