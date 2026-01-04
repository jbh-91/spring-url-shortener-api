package com.julian_heinen.url_shortener_api.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Atomically increments the access count and updates the last access timestamp.
     * <p>
     * <b>Concurrency Note:</b> This method executes a direct database update
     * ({@code UPDATE ... SET count = count + 1}). This prevents "Lost Update" race
     * conditions that would occur if we used the standard "Read-Modify-Write"
     * pattern in Java under high load.
     * </p>
     *
     * @param id           The primary key of the {@link UrlMapping} to update.
     * @param lastAccessed The timestamp to set as the last access time.
     */
    @Modifying
    @Transactional
    @Query("UPDATE UrlMapping u SET u.accessCount = u.accessCount + 1, u.lastAccessed = :lastAccessed WHERE u.id = :id")
    void incrementAccessStats(@Param("id") Long id, @Param("lastAccessed") LocalDateTime lastAccessed);
}
