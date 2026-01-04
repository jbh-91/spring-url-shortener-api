package com.julian_heinen.url_shortener_api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Database entity representing a URL mapping.
 * <p>
 * This class stores the core data for the URL shortening service, linking a
 * unique ID (which is typically encoded to generate the short code) to the
 * original long URL.
 * It also tracks usage statistics like access counts and expiration timestamps.
 * </p>
 */
@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class UrlMapping {

    /**
     * Unique primary key.
     * <p>
     * This numerical ID serves as the seed for generating the Base62 short code.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The original URL.
     * <p>
     * Stored as a Large Object (LOB) to support URLs that exceed standard database
     * string limits (e.g., > 255 characters).
     * </p>
     */
    @Lob
    @NonNull // Generates constructor argument via @RequiredArgsConstructor
    private String originalUrl;

    /**
     * Tracks how often this short URL has been visited.
     * <p>
     * Initialized to 0.
     * </p>
     */
    private int accessCount = 0;

    /**
     * Timestamp of the last successful redirection.
     * <p>
     * Can be {@code null} if the URL has never been accessed.
     * </p>
     */
    private LocalDateTime lastAccessed;

    /**
     * Expiration timestamp for this mapping.
     * <p>
     * If {@code null}, the URL is considered permanent.
     * If the current time is after this timestamp, the URL is considered expired.
     * </p>
     */
    private LocalDateTime expiresAt;

    /**
     * Custom constructor used by Lombok's {@link Builder}.
     * <p>
     * Allows creating instances with specific expiration settings while leaving
     * statistics (accessCount, lastAccessed) at their default values.
     * </p>
     *
     * @param originalUrl The long URL to store.
     * @param expiresAt   The expiration date (or null for permanent).
     */
    @Builder
    public UrlMapping(@NonNull String originalUrl, LocalDateTime expiresAt) {
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
    }
}
