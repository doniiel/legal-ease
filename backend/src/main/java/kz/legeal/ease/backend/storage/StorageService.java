package kz.legeal.ease.backend.storage;

import java.time.Duration;

/**
 * Abstraction for S3-compatible object storage operations.
 *
 * <p>Implementations may target AWS S3, MinIO, or any other compatible backend.
 * All methods operate on a pre-configured bucket (from {@code storage.s3.bucket}).
 */
public interface StorageService {

    /**
     * Upload raw bytes under the given object key.
     *
     * @param key         S3 object key (e.g. {@code "documents/42/42.pdf"})
     * @param data        file content
     * @param contentType MIME type (e.g. {@code "application/pdf"})
     * @return the stored object key
     */
    String uploadFile(String key, byte[] data, String contentType);

    /**
     * Download a stored object and return its raw bytes.
     *
     * @param key S3 object key
     * @return raw file bytes
     */
    byte[] downloadFile(String key);

    /**
     * Permanently delete an object from the bucket.
     *
     * @param key S3 object key
     */
    void deleteFile(String key);

    /**
     * Generate a time-limited presigned GET URL for direct client access.
     *
     * @param key    S3 object key
     * @param expiry URL validity period
     * @return absolute URL string accessible by HTTP clients
     */
    String generatePresignedUrl(String key, Duration expiry);
}
