package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiFinalReviewHandler extends RuleHandler {

    private final RuleAiService aiService;

    @Override
    protected void handle(RuleChainContext context) {
        if (!context.isValid()) {
            context.setAiEnrichment(
                    "Документ содержит ошибки валидации.",
                    "Исправьте все ошибки перед завершением."
            );
            return;
        }

        final var documentText = context.getInput().getDocumentText();
        final var review = aiService.finalReview(documentText, context);

        context.setAiEnrichment(review.getSummary(), review.getRecommendation());
    }

    @Override
    public String name() {
        return "AI_FINAL_REVIEW";
    }
}
