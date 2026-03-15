package kz.legeal.ease.backend.service.rule.engine;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainBuilder;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.context.RuleContext;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Entry point for the rule engine — delegates to the appropriate handler chain.
 *
 * <h2>Three processing pipelines</h2>
 * <ul>
 *   <li>{@link #match} — Template matching (intent detection → keyword scoring → AI re-ranking)</li>
 *   <li>{@link #suggestFields} — Field suggestions (conditional fields → required docs → AI hints)</li>
 *   <li>{@link #complete} — Document completion (validation → risk → required docs → AI final review)</li>
 * </ul>
 *
 * <p>Each pipeline is assembled as a Chain-of-Responsibility by {@link RuleChainBuilder}.
 * Handlers communicate through {@link kz.legeal.ease.backend.service.rule.chain.RuleChainContext}.
 * Results are mapped into a unified {@link RuleEngineResult}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final RuleChainBuilder chainBuilder;

    public RuleEngineResult match(RuleContext context) {
        return run(chainBuilder.buildMatchingChain(), context);
    }

    public RuleEngineResult suggestFields(RuleContext context) {
        return run(chainBuilder.buildFieldSuggestionChain(), context);
    }

    public RuleEngineResult complete(RuleContext context) {
        return run(chainBuilder.buildCompleteChain(), context);
    }

    private RuleEngineResult run(RuleHandler chain, RuleContext context) {
        final var ctx = new RuleChainContext(context);
        chain.process(ctx);
        return toResult(ctx);
    }

    private RuleEngineResult toResult(RuleChainContext ctx) {
        return RuleEngineResult.builder()
                .matchedTemplates(ctx.getMatchedTemplates())
                .validationErrors(ctx.getValidationErrors())
                .risks(ctx.getRisks())
                .requiredDynamicFields(ctx.getRequiredDynamicFields())
                .requiredDocuments(ctx.getRequiredDocuments())
                .fieldSuggestions(ctx.getFieldSuggestions())
                .valid(ctx.isValid())
                .aborted(ctx.isAborted())
                .abortReason(ctx.getAbortReason())
                .aiSummary(ctx.getAiSummary())
                .aiRecommendation(ctx.getAiRecommendation())
                .aiIntentLabel(ctx.getAiIntentLabel())
                .aiIntentConfidence(ctx.getAiIntentConfidence())
                .build();
    }

}
