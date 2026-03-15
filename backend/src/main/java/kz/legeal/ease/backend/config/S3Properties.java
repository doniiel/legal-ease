package kz.legeal.ease.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the S3-compatible object storage (MinIO).
 *
 * <p>Bound from the {@code storage.s3} prefix in {@code application.yml}.
 * In Docker Compose the {@code endpoint} is the internal hostname
 * ({@code http://minio:9000}) while {@code publicEndpoint} is what
 * browsers/clients reach ({@code http://localhost:9000}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "storage.s3")
public class S3Properties {

    /** Internal S3 endpoint used by the backend for upload/download (e.g. http://minio:9000). */
    private String endpoint;

    /**
     * Public endpoint embedded in presigned URLs returned to clients.
     * Defaults to {@code endpoint} when not overridden.
     */
    private String publicEndpoint;

    private String accessKey;

    private String secretKey;

    /** S3 bucket name — created automatically on startup if absent. */
    private String bucket;

    /** Presigned URL validity period in minutes. */
    private int presignExpiryMinutes = 60;

    /** Maximum allowed upload size in bytes (default: 20 MB). */
    private long maxFileSizeBytes = 20 * 1024 * 1024L;
}
