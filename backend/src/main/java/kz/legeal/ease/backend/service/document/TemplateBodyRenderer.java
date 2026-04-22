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
 *   {{field_key}}   — double curly braces
 *   [field_key]     — square brackets (natural lawyer format)
 * </pre>
 *
 * <h3>Conditionals</h3>
 * <pre>
 *   {{#if field_key}}...content...{{/if}}
 * </pre>
 * If {@code field_key} has a non-blank value the content is kept (with its own
 * placeholders substituted); otherwise the entire block including the tags is removed.
 *
 * <h3>Missing values</h3>
 * If a placeholder key has no corresponding filled value it is replaced with
 * {@code _______} so the gap is clearly visible in the PDF as a blank line to fill.
 */
@Service
public class TemplateBodyRenderer {

    /** {{#if key}}...{{/if}} — DOTALL so content can span multiple lines. */
    private static final Pattern CONDITIONAL = Pattern.compile(
            "\\{\\{#if\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\}\\}(.*?)\\{\\{/if\\}\\}",
            Pattern.DOTALL
    );

    /**
     * Combined placeholder pattern — only valid identifier characters:
     *   group 1 — {{field_key}}
     *   group 2 — [field_key]
     * Does NOT match directive tokens like {{#if ...}} or {{/if}}.
     */
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "\\{\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}\\}" +
            "|" +
            "\\[([a-zA-Z_][a-zA-Z0-9_]*)\\]"
    );

    /** Shown in the PDF when a field was left blank. */
    private static final String BLANK_FILL = "_______";

    /**
     * Render the template body:
     * <ol>
     *   <li>Resolve {@code {{#if key}}...{{/if}}} conditionals.</li>
     *   <li>Substitute {@code {{field_key}}} / {@code [field_key]} placeholders.</li>
     * </ol>
     */
    public String render(String body, Map<String, String> fieldValues) {
        if (body == null || body.isBlank()) return "";

        // Pass 1 — conditionals
        body = resolveConditionals(body, fieldValues);

        // Pass 2 — placeholder substitution
        final Matcher      matcher = PLACEHOLDER.matcher(body);
        final StringBuffer result  = new StringBuffer();

        while (matcher.find()) {
            final String key = matcher.group(1) != null
                    ? matcher.group(1).trim()
                    : matcher.group(2);

            final String value = fieldValues.get(key);
            final String replacement = (value != null && !value.isBlank())
                    ? value
                    : BLANK_FILL;
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Extract all unique placeholder keys (for template validation).
     * Supports both {@code {{key}}} and {@code [key]} syntaxes.
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

    // ── helpers ───────────────────────────────────────────────────────────────

    private String resolveConditionals(String body, Map<String, String> fieldValues) {
        final Matcher      m  = CONDITIONAL.matcher(body);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final String key     = m.group(1);
            final String content = m.group(2);
            final String value   = fieldValues.get(key);
            // Keep content (with its own placeholders) if field is filled; else remove block
            m.appendReplacement(sb,
                    Matcher.quoteReplacement(
                            (value != null && !value.isBlank()) ? content : ""));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
