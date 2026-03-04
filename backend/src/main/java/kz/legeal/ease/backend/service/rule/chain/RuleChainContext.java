package kz.legeal.ease.backend.service.rule.chain;

import kz.legeal.ease.backend.service.rule.common.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.RuleContext;
import org.springframework.boot.context.properties.bind.validation.ValidationErrors;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class RuleChainContext {

    private final RuleContext input;

    private final List<MatchedTemplate> matchedTemplates = new ArrayList<>();
    private final List<ValidationError> validationErrors = new ArrayList<>();
    private final List<RiskItem> risks = new ArrayList<>();
    private final List<String> requiredDynamicFields = new ArrayList<>();
    private final List<RequiredDocument> requiredDocuments = new ArrayList<>();
    private final List<FieldSuggestion> fieldSuggestions = new ArrayList<>();

    private String aiSummary;
    private String aiRecommendation;
    private String aiIntentLabel;
    private double aiIntentConfidence;

    private boolean aborted;
    private String abortReason;

    public RuleChainContext(RuleContext input) {
        this.input = input;
    }

    public void addMatch(MatchedTemplate t) {
        matchedTemplates.add(t);
    }

    public void addError(ValidationErrors e) {
        validationErrors.add(e);
    }

    public void addRisk(RiskItem r) {
        risks.add(r);
    }

    public void addDynamicField(String fieldKey) {
        requiredDynamicFields.add(fieldKey);
    }

    public void addRequiredDoc(RequiredDocument d) {
        requiredDocuments.add(d);
    }

    public void addSuggestion(FieldSuggestion s) {
        fieldSuggestions.add(s);
    }

    public void setIntent(String label, double confidence) {
        this.aiIntentLabel = label;
        this.aiIntentConfidence = confidence;
    }

    public void setAiEnrichment(String summary, String recommendation) {
        this.aiSummary = summary;
        this.aiRecommendation = recommendation;
    }

    public void abort(String reason) {
        log.warn("Chain aborted: {}", reason);
        this.aborted = true;
        this.abortReason = reason;
    }

    public boolean isValid() {
        return validationErrors.isEmpty();
    }

    public boolean hasRisks() {
        return !risks.isEmpty();
    }
}

}
