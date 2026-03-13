package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AIUnderstandingHandler extends RuleHandler {

    private final RuleAiService aiService;

    @Override
    public void handle(RuleChainContext context) {
        final var text = context.getInput().getInputText();
        if (text == null || text.isBlank()) return;

        final var result = aiService.detectIntent(text);
        context.setIntent(result.getIntentLabel(), result.getConfidence());
    }

    @Override
    public String name() {
        return "AI_UNDERSTANDING";
    }
}
