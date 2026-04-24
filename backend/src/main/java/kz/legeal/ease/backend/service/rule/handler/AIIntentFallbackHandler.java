package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.MatchedTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback handler for template matching.
 *
 * <p>When keyword-based matching returns no results but the AI detected a
 * clear intent (confidence ≥ 0.5), this handler searches published templates
 * by category name using the intent label as a hint.
 *
 * <p>Runs after {@link MatchingHandler} in the matching chain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIIntentFallbackHandler extends RuleHandler {

    private static final double MIN_CONFIDENCE = 0.5;
    private static final int FALLBACK_BASE_SCORE = 50;

    /**
     * Maps AI intent labels to Russian keywords that appear in category names.
     */
    private static final Map<String, String> INTENT_CATEGORY_KEYWORDS = Map.of(
            "RENT",              "аренда",
            "EMPLOYMENT",        "трудов",
            "PURCHASE",          "купл",
            "POWER_OF_ATTORNEY", "доверенн",
            "LOAN",              "займ",
            "SERVICE",           "услуг",
            "PARTNERSHIP",       "партнёр"
    );

    private final TemplateRepository templateRepository;

    @Override
    public void handle(RuleChainContext context) {
        if (!context.getMatchedTemplates().isEmpty()) return;

        final var intent     = context.getAiIntentLabel();
        final var confidence = context.getAiIntentConfidence();

        if (intent == null || intent.isBlank() || "OTHER".equals(intent)) return;
        if (confidence < MIN_CONFIDENCE) return;

        final var keyword = INTENT_CATEGORY_KEYWORDS.get(intent);
        if (keyword == null) return;

        final var templates = templateRepository.findPublishedByCategoryKeyword(keyword);
        if (templates.isEmpty()) {
            log.debug("AIIntentFallback: no templates found for intent={} keyword={}", intent, keyword);
            return;
        }

        log.debug("AIIntentFallback: intent={} confidence={} → {} template(s) via keyword={}",
                intent, confidence, templates.size(), keyword);

        templates.forEach(t -> context.addMatch(new MatchedTemplate(
                t.getId(),
                t.getTitle(),
                t.getCategory().getName(),
                FALLBACK_BASE_SCORE,
                "Подобрано на основе вашего запроса (AI: " + intent + ")"
        )));
    }

    @Override
    public String name() {
        return "AI_INTENT_FALLBACK";
    }
}
