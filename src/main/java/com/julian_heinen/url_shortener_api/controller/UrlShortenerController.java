package com.julian_heinen.url_shortener_api.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian_heinen.url_shortener_api.dto.CreateUrlRequest;
import com.julian_heinen.url_shortener_api.dto.UrlResponse;
import com.julian_heinen.url_shortener_api.dto.UrlStatsResponse;
import com.julian_heinen.url_shortener_api.service.UrlShortenerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing URL shortening operations.
 * <p>
 * This controller exposes endpoints to create new short URLs, resolve them
 * (redirect), retrieve usage statistics, and delete existing mappings.
 * </p>
 */
@RestController
@RequestMapping("/")
@Validated
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService service;

    /**
     * Creates a new short URL.
     * <p>
     * Accepts a JSON payload containing the original URL and an optional TTL.
     * Returns the generated short URL and metadata.
     * </p>
     *
     * @param request The request body containing the URL to shorten and optional
     *                settings.
     * @return HTTP 201 (Created) with the {@link UrlResponse} body and Location
     *         header.
     */
    @PostMapping
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody @Valid CreateUrlRequest request) {
        UrlResponse response = service.getShortUrl(
                request.url(),
                request.hoursTTL());

        return ResponseEntity
                .created(URI.create(response.shortUrl()))
                .body(response);
    }

    /**
     * Redirects the client to the original URL associated with the provided short
     * code.
     * <p>
     * This endpoint is the core functionality of the shortener. Accessing it
     * triggers a statistical update (access count increment) and validates
     * expiration.
     * </p>
     *
     * @param shortCode The unique identifier for the short URL.
     * @return HTTP 302 (Found) with the {@code Location} header set to the original
     *         URL.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getUrlById(@PathVariable String shortCode) {
        String originalUrl = service.resolveUrl(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    /**
     * Retrieves usage statistics for a specific short URL.
     *
     * @param shortCode The unique identifier for the short URL.
     * @return HTTP 200 (OK) with {@link UrlStatsResponse} containing access counts
     *         and expiration info.
     */
    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getStatsById(@PathVariable String shortCode) {
        UrlStatsResponse response = service.getUrlStats(shortCode);

        return ResponseEntity
                .ok()
                .body(response);
    }

    /**
     * Deletes a short URL mapping.
     * <p>
     * Removes the mapping from the database. Subsequent attempts to access this
     * short code will result in a 404 Not Found error.
     * </p>
     *
     * @param shortCode The unique identifier for the short URL to delete.
     * @return HTTP 204 (No Content) upon successful deletion.
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrlById(@PathVariable String shortCode) {
        service.deleteUrl(shortCode);

        return ResponseEntity
                .noContent()
                .build();
    }
}