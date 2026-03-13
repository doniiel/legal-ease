package kz.legeal.ease.backend.service.rule.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredDocument {

    private Long templateId;

    private String title;

    private String reason;

    private boolean mandatory;
}
