package com.julian_heinen.url_shortener_api.exception;

public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String shortCode) {
        super("URL for shortCode '" + shortCode + "' not found.");
    }
}
