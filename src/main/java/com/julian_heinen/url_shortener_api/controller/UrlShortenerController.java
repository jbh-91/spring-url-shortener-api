package com.julian_heinen.url_shortener_api.controller;

import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julian_heinen.url_shortener_api.service.UrlShortenerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping // not specifying a path here to allow easy configuration in the future
@Validated
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody @URL String longUrl) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlShortenerService.shortenUrl(longUrl));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> getUrlById(@PathVariable String shortUrl) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, urlShortenerService.resolveUrl(shortUrl))
                .build();
    }
}