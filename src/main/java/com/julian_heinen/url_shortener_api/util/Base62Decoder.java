package com.julian_heinen.url_shortener_api.util;

public class Base62Decoder {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static long decode(String shortUrl) {
        long id = 0;

        for (char c : shortUrl.toCharArray()) {
            int value = ALPHABET.indexOf(c);
            id = id * 62 + value;
        }
        return id;
    }
}
