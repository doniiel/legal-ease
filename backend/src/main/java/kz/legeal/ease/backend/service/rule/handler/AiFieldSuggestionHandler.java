package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiFieldSuggestionHandler extends RuleHandler {

    private final RuleAiService aiService;

    @Override
    protected void handle(RuleChainContext context) {
        final var fieldValues = context.getInput().getFieldValues();
        if (fieldValues == null || fieldValues.isEmpty()) return;

        final var hasEmpty = fieldValues.values().stream().anyMatch(String::isBlank);
        if (!hasEmpty) return;

        final var result = aiService.suggestFields(context);
        result.suggestions().forEach(context::addSuggestion);
    }

    @Override
    public String name() {
        return "AI_FIELD_SUGGESTION";
    }
}
