package com.julian_heinen.url_shortener_api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO (Data Transfer Object) for structured error responses.
 * <p>
 * This record is used to return consistent error information to the client
 * whenever an exception occurs within the API.
 * </p>
 *
 * @param requestedUrl  The URL or resource path that was requested when the
 *                      error occurred.
 * @param errorType     A human-readable description or category of the error
 *                      (e.g., "Invalid Argument").
 * @param errorDateTime The timestamp when the error occurred.
 * @param httpStatus    The HTTP status code associated with this error.
 *                      <br>
 *                      Must be a valid status code between 100 and 599.
 */
public record ErrorResponse(
        String requestedUrl,
        String errorType,
        LocalDateTime errorDateTime,
        @Min(100) @Max(599) int httpStatus) {
}
