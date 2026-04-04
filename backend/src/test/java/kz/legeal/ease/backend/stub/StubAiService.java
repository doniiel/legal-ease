package kz.legeal.ease.backend.stub;

import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.result.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * No-op AI service used in {@code test} profile to avoid Anthropic API calls.
 */
@Service
@Profile("test")
public class StubAiService implements RuleAiService {

    @Override
    public IntentResult detectIntent(String inputText) {
        return new IntentResult("Договор", 0.9, new String[]{"contract"});
    }

    @Override
    public RankingResult rankTemplates(RuleChainContext context) {
        return new RankingResult(Map.of(), Map.of());
    }

    @Override
    public EnrichResult enrichResult(RuleChainContext context) {
        return new EnrichResult("stub summary", "stub recommendation");
    }

    @Override
    public SuggestResult suggestFields(RuleChainContext context) {
        return new SuggestResult(java.util.List.of());
    }

    @Override
    public ReviewResult finalReview(String documentText, RuleChainContext context) {
        return new ReviewResult(true, "Document looks fine.", "No issues found.");
    }

    @Override
    public DocsExplainResult explainRequiredDocs(RuleChainContext context) {
        return new DocsExplainResult(Map.of());
    }

    @Override
    public String explainClause(String clauseText) {
        return "This clause means: " + clauseText.substring(0, Math.min(50, clauseText.length()));
    }
}
