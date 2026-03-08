package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.repository.RiskRuleRepository;
import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.RiskItem;
import kz.legeal.ease.backend.service.rule.evaluator.RuleConditionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RiskHandler extends RuleHandler {

    private final RiskRuleRepository riskRuleRepository;
    private final RuleConditionEvaluator evaluator;

    @Override
    protected void handle(RuleChainContext context) {
        final var templateId  = context.getInput().getTemplateId();
        final var fieldValues = context.getInput().getFieldValues();

        if (templateId == null) return;

        final var rules = riskRuleRepository.findAllByTemplateIdAndActiveTrue(templateId);

        rules.forEach(rule -> {
            final var actual  = fieldValues == null || rule.getFieldKey() == null
                    ? ""
                    : fieldValues.getOrDefault(rule.getFieldKey(), "");

            final var isRisky = evaluator.evaluate(actual, rule.getOperator(), rule.getExpectedValue());

            if (isRisky) {
                context.addRisk(new RiskItem(
                        rule.getRuleCode(),
                        rule.getRiskMessage(),
                        rule.getRiskLevel(),
                        null
                ));
            }
        });
    }

    @Override
    public String name() { return "RISK"; }
}
