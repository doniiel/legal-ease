package kz.legeal.ease.backend.dto;

import kz.legeal.ease.backend.dto.document.DocumentDto;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteDocumentResponseDto {

    private boolean success;

    private DocumentDto document;

    private RuleEngineResult result;

    public static CompleteDocumentResponseDto success(DocumentDto document, RuleEngineResult result) {
        return CompleteDocumentResponseDto.builder()
                .success(true)
                .document(document)
                .result(result)
                .build();
    }

    public static CompleteDocumentResponseDto failed(RuleEngineResult result) {
        return CompleteDocumentResponseDto.builder()
                .success(false)
                .document(null)
                .result(result)
                .build();
    }
}
