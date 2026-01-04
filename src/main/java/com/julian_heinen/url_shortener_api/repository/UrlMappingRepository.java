package com.julian_heinen.url_shortener_api.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julian_heinen.url_shortener_api.model.UrlMapping;

/**
 * Repository interface for managing {@link UrlMapping} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide standard CRUD
 * operations (Create, Read, Update, Delete) without requiring boilerplate code.
 * It also defines custom query methods derived from method names (Spring Data
 * JPA).
 * </p>
 */
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Deletes all URL mappings that have an expiration date strictly before the
     * provided timestamp.
     * <p>
     * This method is typically used by scheduled cleanup tasks to remove stale or
     * expired links from the database to free up storage.
     * </p>
     *
     * @param dateTime The reference timestamp. All mappings expiring before this
     *                 time will be deleted.
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
