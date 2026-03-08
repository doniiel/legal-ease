package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.RiskItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiRiskExplainerHandler extends RuleHandler {

    private final RuleAiService aiService;

    @Override
    protected void handle(RuleChainContext context) {
        if (!context.hasRisks()) return;

        final var enriched = aiService.enrichResult(context);

        final List<RiskItem> withExplanation = context.getRisks().stream()
                .map(r -> new RiskItem(r.getRuleCode(), r.getMessage(), r.getLevel(), enriched.getSummary()))
                .toList();

        context.getRisks().clear();
        context.getRisks().addAll(withExplanation);
    }

    @Override
    public String name() { return "AI_RISK_EXPLAINER"; }
}
