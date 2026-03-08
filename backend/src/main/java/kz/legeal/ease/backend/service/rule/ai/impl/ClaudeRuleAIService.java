package kz.legeal.ease.backend.service.rule.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.legeal.ease.backend.service.rule.ai.RuleAiService;
import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import kz.legeal.ease.backend.service.rule.common.FieldSuggestion;
import kz.legeal.ease.backend.service.rule.result.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class ClaudeRuleAIService implements RuleAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-opus-4-6";

    @Override
    public IntentResult detectIntent(String inputText) {
        final var prompt = """
                    Ты — юридический ассистент. Определи намерение пользователя.
                Текст: "%s"
                Ответь СТРОГО в JSON без markdown:
                {
                  "intentLabel": "RENT|EMPLOYMENT|PURCHASE|POWER_OF_ATTORNEY|OTHER",
                  "confidence": 0.95,
                  "keywords": ["ключ1", "ключ2"]
                }
                """.formatted(inputText);
        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            return new IntentResult(
                    node.get("intentLabel").asText(),
                    node.get("confidence").asDouble(),
                    objectMapper.convertValue(node.get("keywords"), String[].class)
            );
        } catch (Exception e) {
            log.warn("detectIntent failed: {}", e.getMessage());
            return new IntentResult("OTHER", 0.0, new String[]{});
        }
    }

    @Override
    public RankingResult rankTemplates(RuleChainContext context) {
        final var templates = context.getMatchedTemplates().stream()
                .map(t -> "id=%d title=%s score=%d".formatted(t.getTemplateId(), t.getTitle(), t.getScore()))
                .toList();

        final var prompt = """
                Входной текст: "%s"
                Намерение: %s (confidence: %.2f)
                Найденные шаблоны: %s
                
                Скорректируй score и добавь пояснение для каждого шаблона.
                Ответь СТРОГО в JSON без markdown:
                {
                  "adjustments": [
                    { "templateId": 1, "scoreDelta": 10, "note": "Точное совпадение по сроку" }
                  ]
                }
                """.formatted(
                context.getInput().getInputText(),
                context.getAiIntentLabel(),
                context.getAiIntentConfidence(),
                templates
        );

        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            final var adjustments = node.get("adjustments");

            final var scores = new java.util.HashMap<Long, Integer>();
            final var notes = new java.util.HashMap<Long, String>();

            adjustments.forEach(a -> {
                scores.put(a.get("templateId").asLong(), a.get("scoreDelta").asInt());
                notes.put(a.get("templateId").asLong(), a.get("note").asText());
            });

            return new RankingResult(scores, notes);
        } catch (Exception e) {
            log.warn("rankTemplates failed: {}", e.getMessage());
            return new RankingResult(Map.of(), Map.of());
        }
    }

    @Override
    public EnrichResult enrichResult(RuleChainContext context) {
        final var prompt = buildEnrichPrompt(context);
        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            return new EnrichResult(
                    node.get("summary").asText(),
                    node.get("recommendation").asText()
            );
        } catch (Exception e) {
            log.warn("enrichResult failed: {}", e.getMessage());
            return new EnrichResult("", "");
        }
    }

    @Override
    public SuggestResult suggestFields(RuleChainContext context) {
        final var fields = context.getInput().getFieldValues().entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getValue().isBlank() ? "[пусто]" : e.getValue()))
                .toList();

        final var prompt = """
            Юридический документ. Поля шаблона: %s
            Предложи стандартные значения для пустых полей на основе юридической практики Казахстана.
            Ответь СТРОГО в JSON без markdown:
            {
              "suggestions": [
                {
                  "fieldKey": "penalty_amount",
                  "label": "Размер штрафа",
                  "suggestedValue": "1 месячный платёж",
                  "reason": "Стандартная практика"
                }
              ]
            }
            """.formatted(fields);

        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            final var list = objectMapper.convertValue(
                    node.get("suggestions"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FieldSuggestion.class)
            );
            return new SuggestResult(list);
        } catch (Exception e) {
            log.warn("suggestFields failed: {}", e.getMessage());
            return new SuggestResult(List.of());
        }
    }

    @Override
    public ReviewResult finalReview(String documentText, RuleChainContext context) {
        final var prompt = """
            Ты — опытный юрист. Проведи финальную проверку документа.
            
            ДОКУМЕНТ:
            %s
            
            Проверь: юридическую корректность, скрытые риски, соответствие законодательству Казахстана.
            Ответь СТРОГО в JSON без markdown:
            {
              "approved": true,
              "summary": "Документ юридически корректен",
              "recommendation": "Рекомендуем нотариально заверить"
            }
            """.formatted(documentText);

        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            return new ReviewResult(
                    node.get("approved").asBoolean(),
                    node.get("summary").asText(),
                    node.get("recommendation").asText()
            );
        } catch (Exception e) {
            log.warn("finalReview failed: {}", e.getMessage());
            return new ReviewResult(true, "", "");
        }
    }

    private String call(String prompt) {
        final var headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var body = Map.of(
                "model", MODEL,
                "max_tokens", 1000,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        final var response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        @SuppressWarnings("unchecked") final var content = (List<Map<String, Object>>) response.getBody().get("content");
        return (String) content.get(0).get("text");
    }

    private String buildEnrichPrompt(RuleChainContext ctx) {
        final var sb = new StringBuilder();
        sb.append("Проанализируй результаты юридической проверки документа.\n\n");

        if (!ctx.getValidationErrors().isEmpty()) {
            sb.append("ОШИБКИ:\n");
            ctx.getValidationErrors().forEach(e ->
                    sb.append("- ").append(e.label()).append(": ").append(e.message()).append("\n"));
        }
        if (!ctx.getRisks().isEmpty()) {
            sb.append("РИСКИ:\n");
            ctx.getRisks().forEach(r ->
                    sb.append("- [").append(r.level()).append("] ").append(r.message()).append("\n"));
        }

        sb.append("""
                Ответь СТРОГО в JSON без markdown:
                {
                  "summary": "краткий анализ (2-3 предложения)",
                  "recommendation": "что делать пользователю"
                }
                """);

        return sb.toString();
    }
}
