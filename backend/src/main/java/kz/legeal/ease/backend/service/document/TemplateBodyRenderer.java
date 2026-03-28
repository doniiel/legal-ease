package kz.legeal.ease.backend.service.document;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders a template body by substituting {@code {{field_key}}} placeholders
 * with actual document field values.
 *
 * <h3>Placeholder syntax</h3>
 * <pre>
 *   {{field_key}}          — exact match, whitespace inside braces is trimmed
 * </pre>
 *
 * <h3>Missing values</h3>
 * If a placeholder key has no corresponding value in the field map, it is
 * replaced with {@code [field_key]} — visually obvious in the rendered
 * document so the user notices the gap rather than seeing blank space.
 *
 * <h3>Example</h3>
 * <pre>
 *   body:   "Договор заключён {{date}} между {{client_name}} и {{company}}."
 *   values: { date="28.03.2026", client_name="Daniyal" }   // company is missing
 *   result: "Договор заключён 28.03.2026 между Daniyal и [company]."
 * </pre>
 */
@Service
public class TemplateBodyRenderer {

    /** Matches {{ anything }} allowing whitespace around the key. */
    private static final Pattern PLACEHOLDER =
            Pattern.compile("\\{\\{\\s*([^}\\s][^}]*)\\s*\\}\\}");

    /**
     * Substitute all placeholders in {@code body} using the provided value map.
     *
     * @param body        template text with {@code {{field_key}}} placeholders
     * @param fieldValues map of fieldKey → fieldValue from the document
     * @return fully rendered text; empty string if {@code body} is null or blank
     */
    public String render(String body, Map<String, String> fieldValues) {
        if (body == null || body.isBlank()) return "";

        final Matcher      matcher = PLACEHOLDER.matcher(body);
        final StringBuffer result  = new StringBuffer();

        while (matcher.find()) {
            final String key         = matcher.group(1).trim();
            final String value       = fieldValues.get(key);
            final String replacement = value != null && !value.isBlank()
                    ? value
                    : "[" + key + "]";   // visually flag missing value
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Extract all unique placeholder keys from a template body, in order of
     * first appearance. Useful for validating that a template's fields cover
     * all placeholders used in its body.
     *
     * @param body template text
     * @return ordered set of placeholder keys; empty if body is null or blank
     */
    public Set<String> extractPlaceholders(String body) {
        final Set<String> keys = new LinkedHashSet<>();
        if (body == null || body.isBlank()) return keys;

        final Matcher matcher = PLACEHOLDER.matcher(body);
        while (matcher.find()) {
            keys.add(matcher.group(1).trim());
        }
        return keys;
    }
}
