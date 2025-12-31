package com.julian_heinen.url_shortener_api.service.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

/**
 * Service zur datenschutzkonformen Anonymisierung und Pseudonymisierung von
 * IP-Adressen.
 * <p>
 * Dieser Service setzt das Prinzip "Privacy by Design" um, indem IP-Adressen
 * <b>vor</b> dem Hashing-Prozess gekürzt (maskiert) werden. Dies verhindert,
 * dass durch Brute-Force-Angriffe (Rainbow Tables) auf den Hash die
 * ursprüngliche Nutzer-IP wiederhergestellt werden kann (k-Anonymität).
 * </p>
 * *
 * <h3>Verwendete Standards zur Maskierung:</h3>
 * <ul>
 * <li><b>IPv4:</b> Das letzte Oktett (8 Bit) wird auf 0 gesetzt.
 * <br>
 * Beispiel: {@code 192.168.123.50} &rarr; {@code 192.168.123.0}</li>
 * <li><b>IPv6:</b> Die letzten 80 Bits werden auf 0 gesetzt (nur die ersten 48
 * Bit / 3 Blöcke bleiben erhalten).
 * <br>
 * Dies entspricht dem Industriestandard von Google Analytics und verhindert
 * Rückschlüsse auf einzelne Anschlüsse (/64 Präfixe).
 * <br>
 * Beispiel: {@code 2001:db8:85a3:0:0:8a2e:370:7334} &rarr;
 * {@code 2001:db8:85a3:0:0:0:0:0}</li>
 * </ul>
 * * @see
 * <a href="https://support.google.com/analytics/answer/2763052?hl=de">Google
 * Analytics IP-Anonymisierung</a>
 */
@Component
public class IpAnonymizationService {

    public String anonymizeAndHash(String rawIp) {
        if (rawIp == null || rawIp.isEmpty()) {
            return "unknown";
        }
        String maskedIp = maskIp(rawIp);
        return hashString(maskedIp);
    }

    private String maskIp(String ip) {
        // TODO: Saubere IP-Validierung einbauen
        if (ip.contains(":")) {
            return maskIPv6(ip);
        } else if (ip.contains(".")) {
            return maskIPv4(ip);
        }
        return ip; // Ungültige IP-Adresse
    }

    private String maskIPv4(String ip) {
        // Letztes Oktett auf 0 setzen
        return ip.substring(0, ip.lastIndexOf('.')) + ".0";
    }

    private String maskIPv6(String ip) {
        // Letzte 80 Bits auf 0 setzen
        String[] parts = ip.split(":");
        if (parts.length > 3) {
            return parts[0] + ":" + parts[1] + ":" + parts[2] + ":0:0:0:0:0";
        }
        return "0:0:0:0:0:0:0:0";
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Erste 8 Zeichen des Hashes zurückgeben
            return bytesToHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException _) {
            return "hash-error";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
