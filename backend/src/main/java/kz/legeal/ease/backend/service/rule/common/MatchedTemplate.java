package kz.legeal.ease.backend.service.rule.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchedTemplate {

    private Long templateId;

    private String title;

    private String categoryName;

    private int score;

    private String aiNote;

}
