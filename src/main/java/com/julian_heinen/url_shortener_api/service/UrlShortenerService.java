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

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    private final String baseUrl;

    private final String serverPort;

    private final int defaultHoursTTL;

    public UrlShortenerService(UrlMappingRepository repository,
            @Value("${app.baseurl}") String baseUrl,
            @Value("${server.port}") String serverPort,
            @Value("${app.default-ttl-hours:0}") int defaultHoursTTL) {
        this.repository = repository;
        this.baseUrl = baseUrl;
        this.serverPort = serverPort;
        this.defaultHoursTTL = defaultHoursTTL;
    }

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

    public String resolveUrl(String shortCode) throws ShortUrlNotFoundException, UrlExpiredException {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        if (isExpired(urlMapping)) {
            throw new UrlExpiredException(shortCode);
        }

        urlMapping.setAccessCount(urlMapping.getAccessCount() + 1);
        urlMapping.setLastAccessed(LocalDateTime.now());
        repository.save(urlMapping);

        return urlMapping.getOriginalUrl();
    }

    public void deleteUrl(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);
        repository.delete(urlMapping);
    }

    public String getOriginalUrl(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return urlMapping.getOriginalUrl();
    }

    public int getAccessCount(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return urlMapping.getAccessCount();
    }

    public LocalDateTime getLastAccessed(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return urlMapping.getLastAccessed();
    }

    public LocalDateTime getExpiresAt(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return urlMapping.getExpiresAt();
    }

    public Boolean isExpired(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return isExpired(urlMapping);
    }

    public UrlStatsResponse getUrlStats(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return new UrlStatsResponse(
                urlMapping.getOriginalUrl(),
                urlMapping.getAccessCount(),
                urlMapping.getLastAccessed(),
                urlMapping.getExpiresAt(),
                isExpired(urlMapping));
    }

    /*
     * Hilfsmethoden
     */

    private String createShortUrlFromId(long id) {
        String shortCode = Base62Encoder.encode(id);

        return baseUrl + ":" + serverPort + "/" + shortCode;
    }

    private UrlMapping getUrlMappingByShortCode(String shortCode) {
        long id = Base62Decoder.decode(shortCode);

        return repository.findById(id)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    private LocalDateTime calculateExpiryDate(int hoursTTL) {
        if (hoursTTL == 0) {
            return null; // Kein Ablaufdatum
        }
        return LocalDateTime.now().plusHours(hoursTTL);
    }

    private Boolean isExpired(UrlMapping urlMapping) {
        LocalDateTime expiresAt = urlMapping.getExpiresAt();
        if (expiresAt == null) {
            return false; // Nie abgelaufen, wenn kein Ablaufdatum gesetzt ist
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}