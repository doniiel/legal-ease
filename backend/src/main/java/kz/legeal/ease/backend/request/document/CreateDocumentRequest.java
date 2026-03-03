package kz.legeal.ease.backend.request.document;

import lombok.Getter;

import java.util.Map;

@Getter
public class CreateDocumentRequest {

    private Long templateId;

    private String title;

    private Map<String, String> fieldValues;
}
