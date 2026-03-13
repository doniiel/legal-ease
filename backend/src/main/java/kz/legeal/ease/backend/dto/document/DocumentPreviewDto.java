package kz.legeal.ease.backend.dto.document;

import kz.legeal.ease.backend.enums.DocumentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentPreviewDto {

    private Long id;

    private String title;

    private String templateTitle;

    private String categoryName;

    private DocumentStatus status;

    private LocalDateTime createdDate;
}
