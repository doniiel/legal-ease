package kz.legeal.ease.backend.service.rule.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchedTemplate {

    private Long templateId;

    private String title;

    private String categoryName;

    private int score;

    private String aiNote;

}
