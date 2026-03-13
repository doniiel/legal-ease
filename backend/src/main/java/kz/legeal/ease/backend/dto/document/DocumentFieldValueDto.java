package kz.legeal.ease.backend.dto.document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentFieldValueDto {

    private Long id;

    private String fieldKey;

    private String fieldValue;
}
