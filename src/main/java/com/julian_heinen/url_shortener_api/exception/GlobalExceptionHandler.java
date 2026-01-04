package com.julian_heinen.url_shortener_api.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.julian_heinen.url_shortener_api.dto.ErrorResponse;
import com.julian_heinen.url_shortener_api.service.security.IpAnonymizationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handler for the API.
 * <p>
 * This class captures specific exceptions thrown by controllers or services
 * and maps them to standardized {@link ErrorResponse} objects with appropriate
 * HTTP status codes.
 * It also handles logging of security-relevant events (e.g., 404s or expired
 * access attempts)
 * with anonymized IP addresses.
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final IpAnonymizationService ipAnonymizer;

    public GlobalExceptionHandler(IpAnonymizationService ipAnonymizer) {
        this.ipAnonymizer = ipAnonymizer;
    }

    /**
     * Handles {@link ConstraintViolationException}, typically thrown when URI
     * parameters fail validation.
     *
     * @param ex      The exception containing validation errors.
     * @param request The current HTTP request.
     * @return A standard error response with HTTP 400 (Bad Request).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid URL provided.",
                request);
    }

    /**
     * Handles {@link MethodArgumentNotValidException}, thrown when DTO validation
     * fails (e.g., @Valid).
     *
     * @param ex      The exception containing field errors.
     * @param request The current HTTP request.
     * @return A standard error response with HTTP 400 (Bad Request) listing
     *         <b>all</b>
     *         validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " - " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Argument provided: " + errorMessages,
                request);
    }

    /**
     * Handles {@link ShortUrlNotFoundException} when a requested short code does
     * not exist.
     * <p>
     * Logs the occurrence with an anonymized client IP hash for security auditing.
     * </p>
     *
     * @param ex      The exception instance.
     * @param request The current HTTP request.
     * @return A standard error response with HTTP 404 (Not Found).
     */
    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotFoundException(
            ShortUrlNotFoundException ex,
            HttpServletRequest request) {

        // Log warning with anonymized user hash
        log.warn("Short URL not found: {}, ClientHash: {}",
                request.getRequestURL(),
                getAnonymizedClientIp(request));

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "The requested short URL doesn't exist.",
                request);
    }

    /**
     * Handles {@link UrlExpiredException} when a short code exists but is no longer
     * valid.
     * <p>
     * Logs the occurrence with an anonymized client IP hash.
     * </p>
     *
     * @param ex      The exception instance.
     * @param request The current HTTP request.
     * @return A standard error response with HTTP 410 (Gone).
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpiredException(
            UrlExpiredException ex,
            HttpServletRequest request) {

        // Log warning with anonymized user hash
        log.warn("Short URL has expired: {}, ClientHash: {}",
                request.getRequestURL(),
                getAnonymizedClientIp(request));

        return buildErrorResponse(
                HttpStatus.GONE,
                "The requested short URL has expired.",
                request);
    }

    // --- Helper Methods ---

    /**
     * Helper method to construct a consistent {@link ErrorResponse}.
     *
     * @param status  The HTTP status to set.
     * @param message The user-facing error message.
     * @param request The original request to extract the path.
     * @return A generic ResponseEntity containing the ErrorResponse.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                request.getRequestURL().toString(),
                message,
                LocalDateTime.now(),
                status.value());

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Extracts and anonymizes the client IP address from the request.
     * <p>
     * Checks headers like "X-Forwarded-For" to handle proxy scenarios.
     * </p>
     *
     * @param request The HTTP request.
     * @return An anonymized hash of the client's IP.
     */
    private String getAnonymizedClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        // Fallback to RemoteAddr if header is not present
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        // If multiple IPs are present (Proxy Chain), take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAnonymizer.anonymizeAndHash(ipAddress);
    }
}
