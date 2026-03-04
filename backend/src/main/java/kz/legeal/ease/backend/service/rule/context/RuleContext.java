package kz.legeal.ease.backend.service.rule.context;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RuleContext {

    private final String inputText;

    private final Long categoryId;

    private final Long templateId;

    private final Map<String, String> fieldValues;

    private final String documentText;

}
