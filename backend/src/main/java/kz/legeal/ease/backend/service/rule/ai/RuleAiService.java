package kz.legeal.ease.backend.service.rule.ai;

import kz.legeal.ease.backend.dto.ai.ClauseExplainResponse;
import kz.legeal.ease.backend.dto.ai.DocumentExplainResponse;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.result.*;

public interface RuleAiService {

    IntentResult detectIntent(String inputText);

    RankingResult rankTemplates(RuleChainContext context);

    EnrichResult enrichResult(RuleChainContext context);

    SuggestResult suggestFields(RuleChainContext context);

    ReviewResult finalReview(String documentText, RuleChainContext context);

    /**
     * Generate user-friendly explanations for each required supporting document.
     * Called by {@link kz.legeal.ease.backend.service.rule.handler.AIDocsExplainerHandler}
     * to enrich the documents list with Kazakhstan-law-aware descriptions.
     */
    DocsExplainResult explainRequiredDocs(RuleChainContext context);

    /**
     * Explain a single legal clause in plain language for a non-lawyer user.
     *
     * @param clauseText raw text of the clause from a contract or legal document
     * @return structured explanation with plain text, risks, and recommendations
     */
    ClauseExplainResponse explainClause(String clauseText);

    /**
     * Explain a completed document in plain language — summary, obligations, warnings, next steps.
     *
     * @param documentText the rendered document text (field labels + values)
     * @param templateTitle the template title for context
     * @return structured plain-language explanation
     */
    DocumentExplainResponse explainDocument(String documentText, String templateTitle);
}
