package kz.legeal.ease.backend.service.rule.chain;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.handler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory that assembles the three rule-engine processing chains.
 *
 * <p>Chains:
 * <ol>
 *   <li><b>Matching</b>: aiUnderstanding → matching → aiIntentFallback → aiRanking</li>
 *   <li><b>Field Suggestion</b>: conditional → requiredDocs → aiDocsExplainer → aiFieldSuggestion</li>
 *   <li><b>Complete</b>: validation → risk → requiredDocs → aiDocsExplainer → aiRiskExplainer → aiFinalReview</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class RuleChainBuilder {

    private final AIUnderstandingHandler   aiUnderstandingHandler;
    private final MatchingHandler          matchingHandler;
    private final AIIntentFallbackHandler  aiIntentFallbackHandler;
    private final AiRankingHandler         aiRankingHandler;

    private final ValidationHandler        validationHandler;
    private final RiskHandler              riskHandler;
    private final ConditionalHandler       conditionalHandler;

    // Required Documents pipeline — inserted into both suggestion and complete chains
    private final RequiredDocsHandler      requiredDocsHandler;
    private final AIDocsExplainerHandler   aiDocsExplainerHandler;

    private final AiFieldSuggestionHandler aiFieldSuggestionHandler;
    private final AiRiskExplainerHandler   aiRiskExplainerHandler;
    private final AiFinalReviewHandler     aiFinalReviewHandler;

    /**
     * Chain for template-matching flow (POST /api/matching).
     * Intent detection → keyword matching → AI intent fallback → AI score re-ranking.
     */
    public RuleHandler buildMatchingChain() {
        return chain(aiUnderstandingHandler, matchingHandler, aiIntentFallbackHandler, aiRankingHandler);
    }

    /**
     * Chain for field-suggestion flow (GET /api/user/documents/{id}/suggestions).
     * Conditional field resolution → required documents + AI explanation → AI field suggestions.
     */
    public RuleHandler buildFieldSuggestionChain() {
        return chain(
                conditionalHandler,
                requiredDocsHandler,
                aiDocsExplainerHandler,
                aiFieldSuggestionHandler
        );
    }

    /**
     * Chain for document-completion flow (POST /api/user/documents/{id}/complete).
     * Validation → risk detection → required documents + AI explanation
     *           → AI risk enrichment → AI final legal review.
     */
    public RuleHandler buildCompleteChain() {
        return chain(
                validationHandler,
                riskHandler,
                requiredDocsHandler,
                aiDocsExplainerHandler,
                aiRiskExplainerHandler,
                aiFinalReviewHandler
        );
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