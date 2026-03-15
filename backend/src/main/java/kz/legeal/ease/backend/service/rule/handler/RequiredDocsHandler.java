package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.repository.RequiredDocRuleRepository;
import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.RequiredDocument;
import kz.legeal.ease.backend.service.rule.evaluator.RuleConditionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Evaluates {@link kz.legeal.ease.backend.domain.RequiredDocRule}s for the current template
 * and populates {@link RuleChainContext#getRequiredDocuments()}.
 *
 * <p>Logic:
 * <ul>
 *   <li>If a rule has no {@code conditionFieldKey} → document is always required.</li>
 *   <li>If a rule has a {@code conditionFieldKey} → document is required only when
 *       {@code fieldValues[conditionFieldKey] (conditionOperator) conditionValue}
 *       evaluates to {@code true}.</li>
 * </ul>
 *
 * <p>Results are later enriched with AI explanations by
 * {@link AIDocsExplainerHandler}.
 */
@Component
@RequiredArgsConstructor
public class RequiredDocsHandler extends RuleHandler {

    private final RequiredDocRuleRepository repository;
    private final RuleConditionEvaluator evaluator;

    @Override
    public void handle(RuleChainContext context) {
        final var templateId = context.getInput().getTemplateId();
        if (templateId == null) return;

        final var fieldValues = context.getInput().getFieldValues();
        final var rules = repository.findAllByTemplateIdAndActiveTrue(templateId);

        rules.forEach(rule -> {
            final boolean shouldRequire;

            if (rule.getConditionFieldKey() == null) {
                // Unconditional: always required
                shouldRequire = true;
            } else {
                // Conditional: evaluate field-based condition
                final var actual = fieldValues == null
                        ? ""
                        : fieldValues.getOrDefault(rule.getConditionFieldKey(), "");
                shouldRequire = evaluator.evaluate(
                        actual,
                        rule.getConditionOperator(),
                        rule.getConditionValue()
                );
            }

            if (shouldRequire) {
                context.addRequiredDoc(new RequiredDocument(
                        null,                      // no internal template reference
                        rule.getRequiredDocTitle(),
                        rule.getReason(),          // may be enriched later by AIDocsExplainerHandler
                        rule.isMandatory()
                ));
            }
        });
    }

    @Override
    public String name() {
        return "REQUIRED_DOCS";
    }
}
