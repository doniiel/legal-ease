package kz.legeal.ease.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Spring configuration for S3-compatible storage (MinIO).
 *
 * <p>Creates two beans:
 * <ul>
 *   <li>{@link S3Client} — used for all upload/download/delete operations
 *       with the <em>internal</em> Docker endpoint ({@code http://minio:9000}).</li>
 *   <li>{@link S3Presigner} — used to generate presigned download URLs using
 *       the <em>public</em> endpoint ({@code http://localhost:9000}) so that
 *       browser clients can reach MinIO directly.</li>
 * </ul>
 *
 * <p>Both MinIO-facing clients require path-style access
 * ({@code s3.example.com/bucket/key} rather than {@code bucket.s3.example.com/key}).
 *
 * <p>On startup the bucket is created if it does not already exist (Step 6 requirement).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    private final S3Properties props;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.US_EAST_1)   // MinIO ignores region; any value is fine
                .credentialsProvider(credentials())
                .serviceConfiguration(s3ServiceConfig())
                .build();
    }

    /**
     * Presigner uses the {@code publicEndpoint} so that the generated URLs
     * are accessible from outside the Docker network.
     */
    @Bean
    public S3Presigner s3Presigner() {
        final var publicEndpoint = (props.getPublicEndpoint() != null && !props.getPublicEndpoint().isBlank())
                ? props.getPublicEndpoint()
                : props.getEndpoint();

        return S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentials())
                .serviceConfiguration(s3ServiceConfig())
                .build();
    }

    /**
     * Step 6 — Ensure the configured bucket exists on application startup.
     * Creates it if absent; does nothing if it already exists.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initBucket() {
        final var bucket = props.getBucket();
        try {
            s3Client().headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("S3 bucket '{}' already exists.", bucket);
        } catch (NoSuchBucketException e) {
            s3Client().createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("S3 bucket '{}' created.", bucket);
        } catch (BucketAlreadyOwnedByYouException e) {
            log.info("S3 bucket '{}' is already owned by this account.", bucket);
        } catch (Exception e) {
            // Non-fatal: log and continue. Storage failures should not prevent startup.
            log.error("Could not initialize S3 bucket '{}': {}", bucket, e.getMessage());
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        );
    }

    private S3Configuration s3ServiceConfig() {
        // MinIO requires path-style access (not virtual-hosted-style)
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }
}
