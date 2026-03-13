package kz.legeal.ease.backend.service.rule.chain;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.handler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleChainBuilder {

    private final AIUnderstandingHandler  aiUnderstandingHandler;
    private final MatchingHandler         matchingHandler;
    private final AiRankingHandler        aiRankingHandler;
    private final ValidationHandler       validationHandler;
    private final RiskHandler             riskHandler;
    private final ConditionalHandler      conditionalHandler;
    private final AiFieldSuggestionHandler aiFieldSuggestionHandler;
    private final AiRiskExplainerHandler  aiRiskExplainerHandler;
    private final AiFinalReviewHandler    aiFinalReviewHandler;

    public RuleHandler buildMatchingChain() {
        return chain(aiUnderstandingHandler, matchingHandler, aiRankingHandler);
    }

    public RuleHandler buildFieldSuggestionChain() {
        return chain(conditionalHandler, aiFieldSuggestionHandler);
    }

    public RuleHandler buildCompleteChain() {
        return chain(validationHandler, riskHandler, aiRiskExplainerHandler, aiFinalReviewHandler);
    }

    private RuleHandler chain(RuleHandler first, RuleHandler... rest) {
        final var head = new ChainLink(first);
        RuleHandler current = head;
        for (final var handler : rest) {
            current = current.setNext(new ChainLink(handler));
        }
        return head;
    }
}