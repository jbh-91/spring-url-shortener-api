package com.julian_heinen.url_shortener_api.exception;

/**
 * Exception thrown when a requested short URL cannot be found in the database.
 * <p>
 * This exception is typically mapped to an HTTP 404 (Not Found) status.
 * </p>
 */
public class ShortUrlNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception for a specific missing short code.
     *
     * @param shortCode The short code that was not found.
     */
    public ShortUrlNotFoundException(String shortCode) {
        super("URL for shortCode '" + shortCode + "' not found.");
    }
}
