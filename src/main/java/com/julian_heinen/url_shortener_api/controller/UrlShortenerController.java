package com.julian_heinen.url_shortener_api.controller;

import java.net.URI;
import java.time.LocalDateTime;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Validated
public class UrlShortenerController {

    private final UrlShortenerService service;

    @PostMapping
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody @Valid CreateUrlRequest request) {
        String shortUrl = service.shortenUrl(request.url());

        UrlResponse response = new UrlResponse(shortUrl, request.url());

        return ResponseEntity
                .created(URI.create(shortUrl))
                .body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getUrlById(@PathVariable String shortCode) {
        String originalUrl = service.resolveUrl(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getStatsById(@PathVariable String shortCode) {
        int accessCount = service.getAccessCount(shortCode);
        String originalUrl = service.getOriginalUrl(shortCode);
        LocalDateTime lastAccessed = service.getLastAccessed(shortCode);

        UrlStatsResponse response = new UrlStatsResponse(originalUrl, accessCount, lastAccessed);

        return ResponseEntity
                .ok()
                .body(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrlById(@PathVariable String shortCode) {
        service.deleteUrl(shortCode);

        return ResponseEntity.noContent().build();
    }
}