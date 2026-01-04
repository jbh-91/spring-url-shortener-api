package com.julian_heinen.url_shortener_api.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) containing usage statistics for a specific short
 * URL.
 * <p>
 * This record provides insights into how often a shortened link has been
 * accessed
 * and its current status (e.g., expiration).
 * </p>
 *
 * @param originalUrl  The original URL associated with the short link.
 * @param accessCount  The total number of times the short URL has been visited.
 * @param lastAccessed The timestamp of the most recent visit.
 *                     <br>
 *                     Can be {@code null} if the URL has never been accessed.
 * @param expiresAt    The timestamp when the URL is scheduled to expire.
 *                     <br>
 *                     Can be {@code null} if no expiration was set.
 * @param isExpired    A derived flag indicating whether the URL is currently
 *                     expired
 *                     based on the {@code expiresAt} timestamp and the current
 *                     server time.
 */
public record UrlStatsResponse(
        String originalUrl,
        int accessCount,
        LocalDateTime lastAccessed,
        LocalDateTime expiresAt,
        Boolean isExpired) {
}
