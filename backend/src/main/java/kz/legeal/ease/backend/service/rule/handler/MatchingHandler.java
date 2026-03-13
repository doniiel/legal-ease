package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.repository.MatchingRuleRepository;
import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.MatchedTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingHandler extends RuleHandler {

    private final MatchingRuleRepository matchingRuleRepository;

    @Override
    public void handle(RuleChainContext context) {
        final var input = context.getInput();
        final var inputText = normalize(input.getInputText());
        final var categoryId = input.getCategoryId();

        final var rules = categoryId != null
                ? matchingRuleRepository.findAllByCategoryIdAndActiveTrue(categoryId)
                : matchingRuleRepository.findAllByActiveTrue();

        rules.forEach(rule -> {
            final var score = calculateScore(inputText, rule.getKeywordList(), rule.getBaseScore());
            if (score > 0) {
                context.addMatch(new MatchedTemplate(
                        rule.getTemplate().getId(),
                        rule.getTemplate().getTitle(),
                        rule.getCategory().getName(),
                        score,
                        null // AI note будет добавлен в AIRankingHandler
                ));
            }
        });

        context.getMatchedTemplates()
                .sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
    }

    private int calculateScore(String inputText, java.util.List<String> keywords, int baseScore) {
        if (inputText == null || inputText.isBlank()) return baseScore / 2;

        final var matched = keywords.stream()
                .filter(inputText::contains)
                .count();

        if (matched == 0) return 0;

        return (int) (baseScore + (matched * 10L));
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase().trim();
    }

    @Override
    public String name() {
        return "MATCHING";
    }
}
