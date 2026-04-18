package kz.legeal.ease.backend.service.document;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders a template body by substituting field-key placeholders with actual
 * document field values.
 *
 * <h3>Supported placeholder syntaxes</h3>
 * <pre>
 *   {{field_key}}   — double curly braces (original format)
 *   [field_key]     — square brackets (natural language format, commonly used by lawyers)
 * </pre>
 * Both syntaxes are recognised and replaced in a single pass. Whitespace inside
 * double-brace placeholders is trimmed; bracket placeholders must use plain
 * snake_case identifiers (letters, digits, underscores — no spaces).
 *
 * <h3>Missing values</h3>
 * If a placeholder key has no corresponding filled value, the original
 * placeholder token is left intact so the gap is visible in the document.
 *
 * <h3>Example</h3>
 * <pre>
 *   body:   "Договор №[contract_number] заключён {{date}} в г. [city]."
 *   values: { contract_number="42", date="28.03.2026" }   // city is missing
 *   result: "Договор №42 заключён 28.03.2026 в г. [city]."
 * </pre>
 */
@Service
public class TemplateBodyRenderer {

    /**
     * Combined pattern:
     *   group 1 — double-brace key  e.g. {{ field_key }}
     *   group 2 — bracket key       e.g. [field_key]
     */
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "\\{\\{\\s*([^}\\s][^}]*)\\s*\\}\\}" +   // {{ field_key }}
            "|" +
            "\\[([a-zA-Z_][a-zA-Z0-9_]*)\\]"          // [field_key]
    );

    /**
     * Substitute all placeholders in {@code body} using the provided value map.
     *
     * @param body        template text with placeholders
     * @param fieldValues map of fieldKey → fieldValue from the document
     * @return fully rendered text; empty string if {@code body} is null or blank
     */
    public String render(String body, Map<String, String> fieldValues) {
        if (body == null || body.isBlank()) return "";

        final Matcher      matcher = PLACEHOLDER.matcher(body);
        final StringBuffer result  = new StringBuffer();

        while (matcher.find()) {
            // group(1) is set for {{...}}, group(2) is set for [...]
            final String key = matcher.group(1) != null
                    ? matcher.group(1).trim()
                    : matcher.group(2);

            final String value = fieldValues.get(key);
            // If value is present use it; otherwise preserve the original token
            final String replacement = (value != null && !value.isBlank())
                    ? value
                    : matcher.group(0);   // leave placeholder as-is so gap is visible
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Extract all unique placeholder keys from a template body, in order of
     * first appearance. Supports both {@code {{key}}} and {@code [key]} syntax.
     *
     * @param body template text
     * @return ordered set of placeholder keys; empty if body is null or blank
     */
    public Set<String> extractPlaceholders(String body) {
        final Set<String> keys = new LinkedHashSet<>();
        if (body == null || body.isBlank()) return keys;

        final Matcher matcher = PLACEHOLDER.matcher(body);
        while (matcher.find()) {
            final String key = matcher.group(1) != null
                    ? matcher.group(1).trim()
                    : matcher.group(2);
            keys.add(key);
        }
        return keys;
    }
}
