package kz.legeal.ease.backend.service.rule.result;

import kz.legeal.ease.backend.service.rule.common.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RuleEngineResult {

    private List<MatchedTemplate> matchedTemplates;
    private List<ValidationError> validationErrors;
    private List<RiskItem> risks;
    private List<String> requiredDynamicFields;
    private List<RequiredDocument> requiredDocuments;
    private List<FieldSuggestion> fieldSuggestions;

    private boolean valid;
    private boolean aborted;
    private String abortReason;

    private String aiSummary;
    private String aiRecommendation;
    private String aiIntentLabel;
    private double aiIntentConfidence;
}
