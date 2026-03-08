package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.repository.ValidationRuleRepository;
import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.ValidationError;
import kz.legeal.ease.backend.service.rule.evaluator.RuleConditionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationHandler extends RuleHandler {

    private final ValidationRuleRepository validationRuleRepository;
    private final RuleConditionEvaluator evaluator;

    @Override
    protected void handle(RuleChainContext context) {
        final var templateId  = context.getInput().getTemplateId();
        final var fieldValues = context.getInput().getFieldValues();

        if (templateId == null || fieldValues == null) return;

        final var rules = validationRuleRepository.findAllByTemplateIdAndActiveTrue(templateId);

        rules.forEach(rule -> {
            final var actual   = fieldValues.getOrDefault(rule.getFieldKey(), "");
            final var violated = !evaluator.evaluate(actual, rule.getOperator(), rule.getExpectedValue());

            if (violated) {
                context.addError(new ValidationError(
                        rule.getFieldKey(),
                        rule.getFieldLabel(),
                        rule.getErrorMessage()
                ));
            }
        });
    }

    @Override
    public String name() { return "VALIDATION"; }
}
