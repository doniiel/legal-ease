    package kz.legeal.ease.backend.service.rule.ai;

    import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
    import kz.legeal.ease.backend.service.rule.result.*;

    public interface RuleAiService {

        IntentResult detectIntent(String inputText);

        RankingResult rankTemplates(RuleChainContext context);

        EnrichResult enrichResult(RuleChainContext context);

        SuggestResult suggestFields(RuleChainContext context);

        ReviewResult finalReview(String documentText, RuleChainContext context);
    }
