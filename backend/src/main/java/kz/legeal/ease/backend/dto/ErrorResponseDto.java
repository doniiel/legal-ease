package kz.legeal.ease.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {

    private String requestId;

    private int status;

    private String message;

    private long timestamp;
}
