package kz.legeal.ease.backend.service.rule.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskItem {

    private String ruleCode;

    private String message;

    private RiskLevel level;

    private String aiExplanation;
}
