package kz.legeal.ease.backend.service.rule.chain;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.handler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleChainBuilder {

    private final AIUnderstandingHandler aiUnderstandingHandler;
    private final MatchingHandler matchingHandler;
    private final AiRankingHandler aiRankingHandler;
    private final ValidationHandler validationHandler;
    private final RiskHandler riskHandler;
    private final ConditionalHandler conditionalHandler;
    private final AiFieldSuggestionHandler aiFieldSuggestionHandler;
    private final AiRiskExplainerHandler aiRiskExplainerHandler;
    private final AiFinalReviewHandler aiFinalReviewHandler;

    public RuleHandler buildMatchingChain() {
        aiUnderstandingHandler
                .setNext(matchingHandler)
                .setNext(aiRankingHandler);
        return aiUnderstandingHandler;
    }

    public RuleHandler buildFieldSuggestionChain() {
        conditionalHandler
                .setNext(aiFieldSuggestionHandler);
        return conditionalHandler;
    }

    public RuleHandler buildCompleteChain() {
        validationHandler
                .setNext(riskHandler)
                .setNext(aiRiskExplainerHandler)
                .setNext(aiFinalReviewHandler);
        return validationHandler;
    }
}
