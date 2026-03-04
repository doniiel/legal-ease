package kz.legeal.ease.backend.service.rule.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldSuggestion {

    private String fieldKey;

    private String label;

    private String suggestion;

    private String reason;
}
