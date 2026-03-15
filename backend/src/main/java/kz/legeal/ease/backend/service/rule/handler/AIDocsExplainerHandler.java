package kz.legeal.ease.backend.service.rule.handler;

import kz.legeal.ease.backend.service.rule.RuleHandler;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.RequiredDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enriches each {@link RequiredDocument} in the context with an AI-generated
 * human-readable explanation of why that document is required under Kazakhstan law.
 *
 * <p>Runs after {@link RequiredDocsHandler} has populated the required-documents list.
 * If the list is empty this handler is a no-op.
 *
 * <p>On AI failure the original lawyer-written {@code reason} is preserved unchanged.
 */
@Component
@RequiredArgsConstructor
public class AIDocsExplainerHandler extends RuleHandler {

    private final RuleAiService aiService;

    @Override
    public void handle(RuleChainContext context) {
        if (context.getRequiredDocuments().isEmpty()) return;

        final var result = aiService.explainRequiredDocs(context);

        // Replace each document's reason with the AI explanation when available
        final List<RequiredDocument> enriched = context.getRequiredDocuments().stream()
                .map(doc -> {
                    final var aiExplanation = result.getExplanations()
                            .getOrDefault(doc.getTitle(), null);
                    // Fall back to original reason if AI did not provide an explanation
                    final var explanation = (aiExplanation != null && !aiExplanation.isBlank())
                            ? aiExplanation
                            : doc.getReason();
                    return new RequiredDocument(
                            doc.getTemplateId(),
                            doc.getTitle(),
                            explanation,
                            doc.isMandatory()
                    );
                })
                .toList();

        context.getRequiredDocuments().clear();
        context.getRequiredDocuments().addAll(enriched);
    }

    @Override
    public String name() {
        return "AI_DOCS_EXPLAINER";
    }
}
