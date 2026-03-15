package kz.legeal.ease.backend.storage.impl;

import kz.legeal.ease.backend.config.S3Properties;
import kz.legeal.ease.backend.exception.StorageException;
import kz.legeal.ease.backend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.Set;

/**
 * {@link StorageService} backed by an S3-compatible endpoint (MinIO in local/Docker dev,
 * AWS S3 in production).
 *
 * <p>All operations target the bucket configured via {@code storage.s3.bucket}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf");

    private final S3Client     s3Client;
    private final S3Presigner  s3Presigner;
    private final S3Properties props;

    @Override
    public String uploadFile(String key, byte[] data, String contentType) {
        validateUpload(data, contentType);

        log.debug("Uploading {} bytes to s3://{}/{}", data.length, props.getBucket(), key);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(props.getBucket())
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) data.length)
                            .build(),
                    RequestBody.fromBytes(data)
            );
        } catch (S3Exception e) {
            throw new StorageException(
                    "Failed to upload object to storage: " + e.awsErrorDetails().errorMessage(),
                    "STORAGE_UPLOAD_FAILED", e
            );
        }

        log.info("Uploaded object: s3://{}/{}", props.getBucket(), key);
        return key;
    }

    @Override
    public byte[] downloadFile(String key) {
        log.debug("Downloading s3://{}/{}", props.getBucket(), key);

        try {
            final ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(props.getBucket())
                            .key(key)
                            .build()
            );
            return response.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new StorageException(
                    "The requested document file was not found in storage.",
                    "STORAGE_FILE_NOT_FOUND", e
            );
        } catch (S3Exception e) {
            throw new StorageException(
                    "Failed to download object from storage: " + e.awsErrorDetails().errorMessage(),
                    "STORAGE_DOWNLOAD_FAILED", e
            );
        }
    }

    @Override
    public void deleteFile(String key) {
        log.info("Deleting s3://{}/{}", props.getBucket(), key);

        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(props.getBucket())
                            .key(key)
                            .build()
            );
        } catch (S3Exception e) {
            throw new StorageException(
                    "Failed to delete object from storage: " + e.awsErrorDetails().errorMessage(),
                    "STORAGE_DELETE_FAILED", e
            );
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration expiry) {
        final var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(r -> r.bucket(props.getBucket()).key(key))
                .build();

        try {
            final var presigned = s3Presigner.presignGetObject(presignRequest);
            final var url = presigned.url().toString();
            log.debug("Generated presigned URL for key '{}', expires in {}", key, expiry);
            return url;
        } catch (S3Exception e) {
            throw new StorageException(
                    "Failed to generate presigned URL: " + e.awsErrorDetails().errorMessage(),
                    "STORAGE_PRESIGN_FAILED", e
            );
        }
    }

    private void validateUpload(byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            throw new StorageException("Cannot upload empty file.", "STORAGE_EMPTY_FILE");
        }
        if (data.length > props.getMaxFileSizeBytes()) {
            throw new StorageException(
                    "File size %d bytes exceeds maximum allowed %d bytes."
                            .formatted(data.length, props.getMaxFileSizeBytes()),
                    "STORAGE_FILE_TOO_LARGE"
            );
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new StorageException(
                    "Content type '%s' is not allowed. Allowed: %s"
                            .formatted(contentType, ALLOWED_CONTENT_TYPES),
                    "STORAGE_INVALID_CONTENT_TYPE"
            );
        }
    }
}
