package kz.legeal.ease.backend.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Time-limited presigned URL for direct S3/MinIO PDF access")
public class PresignedUrlResponse {

    @Schema(description = "Direct download URL valid for expiresInMinutes minutes")
    private String url;

    @Schema(description = "Number of minutes until the presigned URL expires", example = "60")
    private int expiresInMinutes;
}
