package com.julian_heinen.url_shortener_api.exception;

/**
 * Exception thrown when a requested short URL exists but has passed its
 * expiration time (TTL).
 * <p>
 * This exception is typically mapped to a HTTP 410 (Gone) status.
 * </p>
 */
public class UrlExpiredException extends RuntimeException {

    /**
     * Constructs a new exception for an expired short code.
     *
     * @param shortCode The short code of the expired URL.
     */
    public UrlExpiredException(String shortCode) {
        super("URL for shortCode '" + shortCode + "' is expired.");
    }
}
