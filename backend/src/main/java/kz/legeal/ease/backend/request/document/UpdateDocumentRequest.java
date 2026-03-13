package kz.legeal.ease.backend.request.document;

import lombok.Getter;

import java.util.Map;

@Getter
public class UpdateDocumentRequest {

    private String title;

    private Map<String, String> fieldValues;
}
