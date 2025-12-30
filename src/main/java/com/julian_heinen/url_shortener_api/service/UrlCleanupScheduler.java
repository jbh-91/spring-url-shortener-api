package com.julian_heinen.url_shortener_api.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian_heinen.url_shortener_api.repository.UrlMappingRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UrlCleanupScheduler {

    private final UrlMappingRepository repository;

    public UrlCleanupScheduler(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "${app.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();

        log.info("Running cleanup of expired URLs at {}", now);

        repository.deleteByExpiresAtBefore(now);

        log.info("Cleanup of expired URLs completed at {}", now);
    }
}
