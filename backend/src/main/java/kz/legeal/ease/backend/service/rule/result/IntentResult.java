package kz.legeal.ease.backend.service.rule.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IntentResult {

    private String intentLabel;

    private double confidence;

    private String[] keywords;
}
