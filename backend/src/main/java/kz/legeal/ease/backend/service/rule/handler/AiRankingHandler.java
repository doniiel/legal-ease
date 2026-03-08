package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.MatchedTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiRankingHandler extends RuleHandler {

    private final RuleAiService aiService;

    @Override
    protected void handle(RuleChainContext context) {
        if (context.getMatchedTemplates().isEmpty()) return;

        final var ranking = aiService.rankTemplates(context);

        final List<MatchedTemplate> adjusted = context.getMatchedTemplates().stream()
                .map(t -> {
                    final var delta = ranking.getScoreAdjustments().getOrDefault(t.getTemplateId(), 0);
                    final var note  = ranking.getNotes().getOrDefault(t.getTemplateId(), null);
                    return new MatchedTemplate(
                            t.getTemplateId(), t.getTitle(), t.getCategoryName(),
                            Math.min(100, t.getScore() + delta), // max score = 100
                            note
                    );
                })
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();

        context.getMatchedTemplates().clear();
        context.getMatchedTemplates().addAll(adjusted);
    }

    @Override
    public String name() { return "AI_RANKING"; }
}
