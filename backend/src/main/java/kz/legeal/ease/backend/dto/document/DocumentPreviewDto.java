package kz.legeal.ease.backend.dto.document;

import kz.legeal.ease.backend.enums.DocumentStatus;

import java.time.LocalDateTime;

public class DocumentPreviewDto {

    private Long id;

    private String title;

    private String templateTitle;

    private String categoryName;

    private DocumentStatus status;

    private LocalDateTime createdDate;
}
