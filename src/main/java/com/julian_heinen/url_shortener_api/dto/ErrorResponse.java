package com.julian_heinen.url_shortener_api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ErrorResponse(String requestedUrl, String errorType, LocalDateTime errorDateTime,
        @Min(100) @Max(599) int httpStatus) {

}
