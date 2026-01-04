package com.julian_heinen.url_shortener_api.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO (Data Transfer Object) for creating a new short URL.
 * <p>
 * Encapsulates the payload required to generate a shortened link.
 * Includes validation constraints to ensure data integrity before processing.
 * </p>
 *
 * @param url      The original URL to be shortened.
 *                 <br>
 *                 Must not be {@code null} and must adhere to a valid URL
 *                 format.
 * @param hoursTTL The desired Time-To-Live (TTL) for the short URL in hours.
 *                 <br>
 *                 Must be zero or positive. If {@code null}, a system default
 *                 will be applied.
 */
public record CreateUrlRequest(
        @NotNull(message = "URL must not be null") @URL(message = "Invalid URL format") String url,
        @PositiveOrZero Integer hoursTTL) {
}
