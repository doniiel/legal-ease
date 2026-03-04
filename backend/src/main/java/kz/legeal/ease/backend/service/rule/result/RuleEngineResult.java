package kz.legeal.ease.backend.service.rule.result;

import kz.legeal.ease.backend.service.rule.common.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class RuleEngineResult {

    private final List<MatchedTemplate> matchedTemplates = new ArrayList<>();
    private final List<ValidationError> validationErrors = new ArrayList<>();
    private final List<RiskItem> risks = new ArrayList<>();
    private final List<String> requiredDynamicFields = new ArrayList<>();
    private final List<RequiredDocument> requiredDocuments = new ArrayList<>();
    private final List<FieldSuggestion> fieldSuggestions = new ArrayList<>();

    private final boolean valid;
    private final boolean aborted;
    private final String abortReason;

    private final String aiSummary;
    private final String aiRecommendation;
    private final String aiIntentLabel;
    private final double aiIntentConfidence;
}
