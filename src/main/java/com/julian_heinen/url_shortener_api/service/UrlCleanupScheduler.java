package com.julian_heinen.url_shortener_api.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian_heinen.url_shortener_api.repository.UrlMappingRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled task for database maintenance.
 * <p>
 * This component runs periodically to remove expired URL mappings from the
 * database, ensuring that storage is not cluttered with invalid links.
 * </p>
 */
@Component
@Slf4j
public class UrlCleanupScheduler {

    private final UrlMappingRepository repository;

    public UrlCleanupScheduler(UrlMappingRepository repository) {
        this.repository = repository;
    }

    /**
     * Identifies and deletes expired URLs.
     * <p>
     * This method is triggered automatically by the scheduler based on the
     * configured Cron expression.
     * It removes all mappings where the {@code expiresAt} timestamp is strictly in
     * the past.
     * The operation is transactional to ensure data consistency during bulk
     * deletion.
     * </p>
     * <p>
     * <b>Configuration:</b>
     * The schedule is defined by {@code app.cleanup.cron} in
     * {@code application.properties}.
     * Default: Daily at 03:00 AM ({@code 0 0 3 * * *}).
     * </p>
     */
    @Scheduled(cron = "${app.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();

        log.info("Running cleanup of expired URLs at {}", now);

        repository.deleteByExpiresAtBefore(now);

        log.info("Cleanup of expired URLs completed at {}", now);
    }
}
