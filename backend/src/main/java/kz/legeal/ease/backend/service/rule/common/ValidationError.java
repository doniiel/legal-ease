package kz.legeal.ease.backend.service.rule.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {

    private String fieldKey;

    private String label;

    private String message;
}
