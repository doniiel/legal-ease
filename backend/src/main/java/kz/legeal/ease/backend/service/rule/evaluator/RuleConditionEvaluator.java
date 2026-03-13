package kz.legeal.ease.backend.service.rule.evaluator;

import kz.legeal.ease.backend.enums.RuleConditionOperator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RuleConditionEvaluator {

    public boolean evaluate(String actualValue, RuleConditionOperator operator, String expectedValue) {
        return switch (operator) {
            case IS_EMPTY         -> isEmpty(actualValue);
            case IS_NOT_EMPTY     -> !isEmpty(actualValue);
            case EQUALS           -> !isEmpty(actualValue) && actualValue.equalsIgnoreCase(expectedValue);
            case NOT_EQUALS       -> !isEmpty(actualValue) && !actualValue.equalsIgnoreCase(expectedValue);
            case CONTAINS         -> !isEmpty(actualValue) && actualValue.toLowerCase()
                    .contains(expectedValue.toLowerCase());
            case GREATER_THAN     -> compareNumbers(actualValue, expectedValue) > 0;
            case LESS_THAN        -> compareNumbers(actualValue, expectedValue) < 0;
            case GREATER_OR_EQUAL -> compareNumbers(actualValue, expectedValue) >= 0;
            case LESS_OR_EQUAL    -> compareNumbers(actualValue, expectedValue) <= 0;
        };
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    private int compareNumbers(String actual, String expected) {
        try {
            return new BigDecimal(actual.trim())
                    .compareTo(new BigDecimal(expected.trim()));
        } catch (NumberFormatException e) {
            return actual.trim().compareToIgnoreCase(expected.trim());
        }
    }
}
