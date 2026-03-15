package kz.legeal.ease.backend.scheduler;

import kz.legeal.ease.backend.repository.DocumentShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Periodic cleanup job that removes expired document share tokens from the database.
 * Runs every hour by default.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShareCleanupScheduler {

    private final DocumentShareRepository documentShareRepository;

    @Scheduled(fixedRateString = "${app.share.cleanup-interval-ms:3600000}")
    @Transactional
    public void deleteExpiredShares() {
        final int deleted = documentShareRepository.deleteExpiredBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Share cleanup: removed {} expired share token(s)", deleted);
        }
    }
}
