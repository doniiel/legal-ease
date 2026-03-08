package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.repository.ConditionalRuleRepository;
import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.evaluator.RuleConditionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConditionalHandler extends RuleHandler {

    private final ConditionalRuleRepository conditionalRuleRepository;
    private final RuleConditionEvaluator evaluator;

    @Override
    protected void handle(RuleChainContext context) {
        final var templateId = context.getInput().getTemplateId();
        final var fieldValues = context.getInput().getFieldValues();

        if (templateId == null || fieldValues == null) return;

        final var rules = conditionalRuleRepository.findAllByTemplateIdAndActiveTrue(templateId);

        rules.forEach(rule -> {
            final var actual = fieldValues.getOrDefault(rule.getConditionFieldKey(), "");
            final var conditionMet = evaluator.evaluate(actual, rule.getOperator(), rule.getConditionValue());

            if (conditionMet) {
                context.addDynamicField(rule.getTargetFieldKey());
            }
        });
    }

    @Override
    public String name() {
        return "CONDITIONAL";
    }
}
