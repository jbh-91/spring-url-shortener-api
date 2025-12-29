package com.julian_heinen.url_shortener_api.exception;

public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String shortCode) {
        super("URL for shortCode '" + shortCode + "' is expired.");
    }
}
