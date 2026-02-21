package kz.legeal.ease.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class EmailMessage {

    private final String to;
    private final String subject;
    private final String templateName;
    private final Map<String, Object> variables;
}
