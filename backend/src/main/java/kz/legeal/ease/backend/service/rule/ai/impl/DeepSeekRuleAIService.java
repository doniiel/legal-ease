package kz.legeal.ease.backend.service.rule.ai.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.legeal.ease.backend.dto.ai.DocumentExplainResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class DeepSeekRuleAIService implements RuleAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL   = "deepseek-chat";

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
                    objectMapper.convertValue(node.get("keywords"),
                            new TypeReference<String[]>() {})
            );
        } catch (Exception e) {
            log.warn("detectIntent failed: {}", e.getMessage());
            return new IntentResult("OTHER", 0.0, new String[]{});
        }
    }

    @Override
    public RankingResult rankTemplates(RuleChainContext context) {
        final var templates = context.getMatchedTemplates().stream()
                .map(t -> "id=%d title=%s score=%d".formatted(
                        t.getTemplateId(), t.getTitle(), t.getScore()))
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
            final var json        = call(prompt);
            final var node        = objectMapper.readTree(json);
            final var adjustments = node.get("adjustments");

            final var scores = new HashMap<Long, Integer>();
            final var notes  = new HashMap<Long, String>();

            adjustments.forEach(a -> {
                scores.put(a.get("templateId").asLong(), a.get("scoreDelta").asInt());
                notes.put(a.get("templateId").asLong(),  a.get("note").asText());
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
            final List<FieldSuggestion> list = objectMapper.convertValue(
                    node.get("suggestions"),
                    new TypeReference<List<FieldSuggestion>>() {}
            );
            return new SuggestResult(list != null ? list : List.of());
        } catch (Exception e) {
            log.warn("suggestFields failed: {}", e.getMessage());
            return new SuggestResult(List.of());
        }
    }

    @Override
    public ReviewResult finalReview(String documentText, RuleChainContext context) {
        final var risksSummary = context.getRisks().isEmpty()
                ? "Риски не обнаружены."
                : context.getRisks().stream()
                        .map(r -> "[%s] %s".formatted(r.getLevel(), r.getMessage()))
                        .reduce("", (a, b) -> a + "\n- " + b);

        final var prompt = """
                Ты — опытный юрист. Проведи финальную проверку документа.

                ДОКУМЕНТ:
                %s

                УЖЕ ОБНАРУЖЕННЫЕ РИСКИ (не дублируй их, учти при анализе):
                %s

                Проверь: юридическую корректность, скрытые риски, соответствие законодательству Казахстана.
                Ответь СТРОГО в JSON без markdown:
                {
                  "approved": true,
                  "summary": "Документ юридически корректен",
                  "recommendation": "Рекомендуем нотариально заверить"
                }
                """.formatted(documentText, risksSummary);

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

    @Override
    public DocsExplainResult explainRequiredDocs(RuleChainContext context) {
        final var docs = context.getRequiredDocuments().stream()
                .map(d -> "- \"%s\" (обязателен: %s): %s"
                        .formatted(d.getTitle(), d.isMandatory() ? "да" : "нет", d.getReason()))
                .toList();

        final var fieldKeys = context.getInput().getFieldValues() != null
                ? context.getInput().getFieldValues().keySet().toString()
                : "[]";

        final var prompt = """
                Ты — юридический ассистент. Пользователь заполняет юридический документ (поля: %s).
                Для завершения необходимы следующие дополнительные документы:
                %s

                Объясни каждый документ простым языком (1-2 предложения), почему он необходим
                по законодательству Казахстана и что будет, если его не предоставить.
                Ответь СТРОГО в JSON без markdown:
                {
                  "explanations": {
                    "Название документа 1": "Объяснение...",
                    "Название документа 2": "Объяснение..."
                  }
                }
                """.formatted(fieldKeys, String.join("\n", docs));

        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            final Map<String, String> explanations = objectMapper.convertValue(
                    node.get("explanations"),
                    new TypeReference<Map<String, String>>() {}
            );
            return new DocsExplainResult(explanations != null ? explanations : Map.of());
        } catch (Exception e) {
            log.warn("explainRequiredDocs failed: {}", e.getMessage());
            return new DocsExplainResult(Map.of());
        }
    }

    @Override
    public kz.legeal.ease.backend.dto.ai.ClauseExplainResponse explainClause(String clauseText) {
        final var prompt = """
                Ты — юридический ассистент для Казахстана.
                Объясни следующий пункт юридического документа простым языком для обычного человека.
                Текст пункта: "%s"
                Ответь СТРОГО в JSON без markdown:
                {
                  "explanation": "Что означает этот пункт (2-3 предложения)",
                  "simplifiedText": "Одно предложение — суть пункта простыми словами",
                  "risks": ["Риск 1 если есть", "Риск 2 если есть"],
                  "recommendations": ["Рекомендация 1", "Рекомендация 2"]
                }
                """.formatted(clauseText);
        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            final var risks = objectMapper.convertValue(node.get("risks"), new TypeReference<List<String>>() {});
            final var recs  = objectMapper.convertValue(node.get("recommendations"), new TypeReference<List<String>>() {});
            return new kz.legeal.ease.backend.dto.ai.ClauseExplainResponse(
                    node.get("explanation").asText(),
                    node.get("simplifiedText").asText(),
                    risks != null ? risks : List.of(),
                    recs  != null ? recs  : List.of()
            );
        } catch (Exception e) {
            log.warn("explainClause failed: {}", e.getMessage());
            return new kz.legeal.ease.backend.dto.ai.ClauseExplainResponse(
                    "Не удалось получить объяснение. Пожалуйста, попробуйте позже.",
                    "", List.of(), List.of()
            );
        }
    }

    @Override
    public DocumentExplainResponse explainDocument(String documentText, String templateTitle) {
        final var prompt = """
                Ты — юридический ассистент для Казахстана. Пользователь заполнил юридический документ.
                Тип документа: %s

                Данные документа:
                %s

                Объясни этот документ простым языком для обычного человека без юридического образования.
                Ответь СТРОГО в JSON без markdown:
                {
                  "summary": "Что это за документ и для чего он нужен (2-3 предложения)",
                  "obligations": "Какие обязательства и права создаёт этот документ для каждой стороны (3-5 пунктов списком)",
                  "warnings": "На что важно обратить внимание, возможные риски и подводные камни (2-3 пункта)",
                  "nextSteps": "Что делать дальше после подписания этого документа (2-3 рекомендации)"
                }
                """.formatted(templateTitle, documentText);
        try {
            final var json = call(prompt);
            final var node = objectMapper.readTree(json);
            return new DocumentExplainResponse(
                    node.get("summary").asText(),
                    node.get("obligations").asText(),
                    node.get("warnings").asText(),
                    node.get("nextSteps").asText()
            );
        } catch (Exception e) {
            log.warn("explainDocument failed: {}", e.getMessage());
            return new DocumentExplainResponse(
                    "Не удалось получить объяснение документа.",
                    "", "", "Пожалуйста, попробуйте позже."
            );
        }
    }

    private String call(String prompt) {
        final var headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var body = Map.of(
                "model",    MODEL,
                "max_tokens", 1000,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        final var response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        @SuppressWarnings("unchecked")
        final var choices = (List<Map<String, Object>>) response.getBody().get("choices");
        @SuppressWarnings("unchecked")
        final var message = (Map<String, Object>) choices.get(0).get("message");
        return stripMarkdown((String) message.get("content"));
    }

    private String stripMarkdown(String raw) {
        if (raw == null) return "";
        String s = raw.strip();
        if (s.startsWith("```")) {
            s = s.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("(?s)```\\s*$", "").strip();
        }
        return s;
    }

    private String buildEnrichPrompt(RuleChainContext ctx) {
        final var sb = new StringBuilder();
        sb.append("Проанализируй результаты юридической проверки документа.\n\n");

        if (!ctx.getValidationErrors().isEmpty()) {
            sb.append("ОШИБКИ:\n");
            ctx.getValidationErrors().forEach(e ->
                    sb.append("- ").append(e.getLabel())
                            .append(": ").append(e.getMessage()).append("\n"));
        }

        if (!ctx.getRisks().isEmpty()) {
            sb.append("РИСКИ:\n");
            ctx.getRisks().forEach(r ->
                    sb.append("- [").append(r.getLevel())
                            .append("] ").append(r.getMessage()).append("\n"));
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
