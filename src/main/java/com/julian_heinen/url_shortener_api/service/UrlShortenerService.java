package com.julian_heinen.url_shortener_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.julian_heinen.url_shortener_api.exception.ShortUrlNotFoundException;
import com.julian_heinen.url_shortener_api.model.UrlMapping;
import com.julian_heinen.url_shortener_api.repository.UrlMappingRepository;
import com.julian_heinen.url_shortener_api.util.Base62Decoder;
import com.julian_heinen.url_shortener_api.util.Base62Encoder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    @Value("${app.baseurl}")
    private String baseUrl;

    @Value("${server.port}")
    private String serverPort;

    public String shortenUrl(String longUrl) {
        UrlMapping urlMapping = repository.save(new UrlMapping(longUrl));
        String shortCode = Base62Encoder.encode(urlMapping.getId());

        return baseUrl + ":" + serverPort + "/" + shortCode;
    }

    public String resolveUrl(String shortCode) {
        UrlMapping urlMapping = getUrlMappingByShortCode(shortCode);

        return urlMapping.getOriginalUrl();
    }

    /*
     * Hilfsmethode
     */
    private UrlMapping getUrlMappingByShortCode(String shortCode) {
        long id = Base62Decoder.decode(shortCode);

        return repository.findById(id)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }
}