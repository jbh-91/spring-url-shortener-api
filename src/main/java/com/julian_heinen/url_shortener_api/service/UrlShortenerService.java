package com.julian_heinen.url_shortener_api.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.julian_heinen.url_shortener_api.dto.UrlResponse;
import com.julian_heinen.url_shortener_api.dto.UrlStatsResponse;
import com.julian_heinen.url_shortener_api.exception.ShortUrlNotFoundException;
import com.julian_heinen.url_shortener_api.exception.UrlExpiredException;
import com.julian_heinen.url_shortener_api.model.UrlMapping;
import com.julian_heinen.url_shortener_api.repository.UrlMappingRepository;
import com.julian_heinen.url_shortener_api.util.Base62Decoder;
import com.julian_heinen.url_shortener_api.util.Base62Encoder;

/**
 * Core service containing the business logic for URL shortening and resolution.
 * <p>
 * This service handles the creation of short URLs, retrieval of original URLs,
 * statistics tracking, and validation of expiration times.
 * It interacts with the {@link UrlMappingRepository} for persistence and uses
 * Base62 encoding/decoding utilities to convert database IDs to short codes.
 * </p>
 */
@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    /**
     * The base domain of the application (e.g., "http://localhost").
     * <br>
     * Configured via {@code app.baseurl} in {@code application.properties}.
     */
    private final String baseUrl;

    /**
     * The port the server is running on.
     * <br>
     * Configured via {@code server.port} in {@code application.properties}.
     */
    private final String serverPort;

    /**
     * Default Time-To-Live (TTL) in hours if not specified by the client.
     * <br>
     * Configured via {@code app.default-ttl-hours} in
     * {@code application.properties}.
     * Defaults to 0 (no expiration).
     */
    private final int defaultHoursTTL;

    /**
     * Constructs the service with dependencies and configuration values.
     *
     * @param repository      The repository for data access.
     * @param baseUrl         Injected from {@code app.baseurl}.
     * @param serverPort      Injected from {@code server.port}.
     * @param defaultHoursTTL Injected from {@code app.default-ttl-hours}.
     */
    public UrlShortenerService(UrlMappingRepository repository,
            @Value("${app.baseurl}") String baseUrl,
            @Value("${server.port}") String serverPort,
            @Value("${app.default-ttl-hours:0}") int defaultHoursTTL) {
        this.repository = repository;
        this.baseUrl = baseUrl;
        this.serverPort = serverPort;
        this.defaultHoursTTL = defaultHoursTTL;
    }

    /**
     * Creates a new short URL mapping.
     *
     * @param originalUrl The long URL to shorten.
     * @param hoursTTL    Optional custom TTL in hours. If null, the system default
     *                    is used.
     * @return A {@link UrlResponse} containing the generated short link and
     *         metadata.
     */
    public UrlResponse getShortUrl(String originalUrl, Integer hoursTTL) {
        int ttl = (hoursTTL != null) ? hoursTTL : defaultHoursTTL;
        LocalDateTime expiresAt = calculateExpiryDate(ttl);

        UrlMapping urlMapping = repository.save(
                UrlMapping.builder()
                        .originalUrl(originalUrl)
                        .expiresAt(expiresAt)
                        .build());

        String shortUrl = createShortUrlFromId(urlMapping.getId());

        return new UrlResponse(shortUrl, originalUrl, expiresAt);
    }

    /**
     * Resolves a short code to the original URL.
     * <p>
     * <b>Side Effects:</b>
     * <ul>
     * <li>Increments the access count for this mapping.</li>
     * <li>Updates the {@code lastAccessed} timestamp.</li>
     * </ul>
     * </p>
     *
     * @param shortCode The short code (path variable) to resolve.
     * @return The original long URL.
     * @throws ShortUrlNotFoundException If the code does not exist in the database.
     * @throws UrlExpiredException       If the mapping exists but has expired.
     */
    public String resolveUrl(String shortCode) throws ShortUrlNotFoundException, UrlExpiredException {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        if (isExpired(urlMapping)) {
            throw new UrlExpiredException(shortCode);
        }

        // Update statistics
        urlMapping.setAccessCount(urlMapping.getAccessCount() + 1);
        urlMapping.setLastAccessed(LocalDateTime.now());
        repository.save(urlMapping);

        return urlMapping.getOriginalUrl();
    }

    /**
     * Deletes a URL mapping by its short code.
     *
     * @param shortCode The identifier of the mapping to delete.
     * @throws ShortUrlNotFoundException If the code does not exist.
     */
    public void deleteUrl(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);
        repository.delete(urlMapping);
    }

    /**
     * Aggregates usage statistics for a given short code.
     *
     * @param shortCode The identifier to fetch stats for.
     * @return A {@link UrlStatsResponse} containing access counts and timestamps.
     */

    public UrlStatsResponse getUrlStats(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return new UrlStatsResponse(
                urlMapping.getOriginalUrl(),
                urlMapping.getAccessCount(),
                urlMapping.getLastAccessed(),
                urlMapping.getExpiresAt(),
                isExpired(urlMapping));
    }

    // --- Utility Getters (Public API helpers) ---

    public String getOriginalUrl(String shortCode) {
        return getUrlMappingByShortCode(shortCode).getOriginalUrl();
    }

    public int getAccessCount(String shortCode) {
        return getUrlMappingByShortCode(shortCode).getAccessCount();
    }

    public LocalDateTime getLastAccessed(String shortCode) {
        return getUrlMappingByShortCode(shortCode).getLastAccessed();
    }

    public LocalDateTime getExpiresAt(String shortCode) {
        return getUrlMappingByShortCode(shortCode).getExpiresAt();
    }

    public Boolean isExpired(String shortCode) {
        return isExpired(getUrlMappingByShortCode(shortCode));
    }

    // --- Helper Methods ---

    /**
     * Generates the full short URL string.
     * <p>
     * Encodes the database ID into Base62 and appends it to the base URL and port.
     * </p>
     */
    private String createShortUrlFromId(long id) {
        String shortCode = Base62Encoder.encode(id);

        return baseUrl + ":" + serverPort + "/" + shortCode;
    }

    /**
     * Decodes the short code to an ID and retrieves the entity.
     *
     * @throws ShortUrlNotFoundException if decoding fails or ID is not found.
     */
    private UrlMapping getUrlMappingByShortCode(String shortCode) {
        long id = Base62Decoder.decode(shortCode);

        return repository.findById(id)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    /**
     * Calculates the expiration date.
     *
     * @param hoursTTL Time to live in hours.
     * @return The expiration timestamp, or {@code null} if hoursTTL is 0
     *         (permanent).
     */
    private LocalDateTime calculateExpiryDate(int hoursTTL) {
        if (hoursTTL == 0) {
            return null; // No expiration
        }
        return LocalDateTime.now().plusHours(hoursTTL);
    }

    /**
     * Checks if a mapping is expired based on the current server time.
     */
    private Boolean isExpired(UrlMapping urlMapping) {
        LocalDateTime expiresAt = urlMapping.getExpiresAt();
        if (expiresAt == null) {
            return false;// Never expires
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}