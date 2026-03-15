package kz.legeal.ease.backend.service.rule.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result produced by {@link kz.legeal.ease.backend.service.rule.ai.RuleAiService#explainRequiredDocs}.
 *
 * <p>Maps each required document title to an AI-generated human-readable explanation
 * of why that document is needed in the context of Kazakhstan law.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocsExplainResult {

    /** Key: requiredDocTitle, Value: AI-generated explanation for the user. */
    private Map<String, String> explanations;
}
