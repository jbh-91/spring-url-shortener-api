package com.julian_heinen.url_shortener_api.dto;

import java.time.LocalDateTime;

public record UrlResponse(String shortUrl, String originalUrl, LocalDateTime expiresAt) {

}
