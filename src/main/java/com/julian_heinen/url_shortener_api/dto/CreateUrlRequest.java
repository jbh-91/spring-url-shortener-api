package com.julian_heinen.url_shortener_api.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotNull;

public record CreateUrlRequest(
        @NotNull(message = "URL must not be null") @URL(message = "Invalid URL format") String url) {
}
