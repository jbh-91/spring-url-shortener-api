package com.julian_heinen.url_shortener_api.dto;

import java.time.LocalDateTime;

public record UrlStatsResponse(String originalUrl, int accessCount, LocalDateTime lastAccessed, LocalDateTime expiresAt,
        Boolean isExpired) {

}
