package com.julian_heinen.url_shortener_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julian_heinen.url_shortener_api.model.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
}
