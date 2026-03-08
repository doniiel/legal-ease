package kz.legeal.ease.backend.dto.document;

import kz.legeal.ease.backend.enums.DocumentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DocumentDto {

    private Long id;

    private String title;

    private Long templateId;

    private String templateTitle;

    private String categoryName;

    private DocumentStatus status;

    private List<DocumentFieldValueDto> fieldValues;

    private List<String> missingRequiredFields;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
