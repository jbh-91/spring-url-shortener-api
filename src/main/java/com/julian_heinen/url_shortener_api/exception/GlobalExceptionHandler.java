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

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final IpAnonymizationService ipAnonymizer;

    public GlobalExceptionHandler(IpAnonymizationService ipAnonymizer) {
        this.ipAnonymizer = ipAnonymizer;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid URL provided.",
                request);
    }

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

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotFoundException(
            ShortUrlNotFoundException ex,
            HttpServletRequest request) {

        // LOGGING: Url und User-Hash (WARN)
        log.warn("Short URL not found: {}, ClientHash: {}",
                request.getRequestURL(),
                getAnonymizedClientIp(request));

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "The requested short URL doesn't exist.",
                request);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpiredException(
            UrlExpiredException ex,
            HttpServletRequest request) {

        // LOGGING: Url und User-Hash (WARN)
        log.warn("Short URL has expired: {}, ClientHash: {}",
                request.getRequestURL(),
                getAnonymizedClientIp(request));

        return buildErrorResponse(
                HttpStatus.GONE,
                "The requested short URL has expired.",
                request);
    }

    /*
     * Hilfsmethoden
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

    private String getAnonymizedClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        // Fallback auf RemoteAddr, falls kein Header gesetzt
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        // Falls mehrere IPs im Header stehen (Proxy-Chain), nimm die erste
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAnonymizer.anonymizeAndHash(ipAddress);
    }
}
