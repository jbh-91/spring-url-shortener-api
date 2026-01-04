package com.julian_heinen.url_shortener_api.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) representing the response after a successful URL
 * creation.
 * <p>
 * This record returns the generated short URL along with the original link and
 * its
 * expiration metadata to the client.
 * </p>
 *
 * @param shortUrl    The complete, shortened URL (e.g.,
 *                    "https://api.xyz/AbCd12").
 * @param originalUrl The original long URL that was shortened.
 * @param expiresAt   The timestamp when this short URL will become invalid.
 *                    <br>
 *                    Can be {@code null} if the URL is set to never expire.
 */
public record UrlResponse(
        String shortUrl,
        String originalUrl,
        LocalDateTime expiresAt) {
}
