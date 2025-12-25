package com.julian_heinen.url_shortener_api.dto;

public record UrlStatsResponse(String originalUrl, int accessCount) {

}
