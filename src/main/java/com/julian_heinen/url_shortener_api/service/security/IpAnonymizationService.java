package com.julian_heinen.url_shortener_api.service.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    /**
     * Maskiert die übergebene IP-Adresse und erstellt einen Hash daraus.
     *
     * @param rawIp Die rohe IP-Adresse (kann null, leer oder ungültig sein).
     * @return Einen 8-stelligen Hex-Hash der maskierten IP oder Fallback-Werte
     *         ({@code "unknown"}, {@code "hash-error"}).
     */
    public String anonymizeAndHash(String rawIp) {
        if (rawIp == null || rawIp.isEmpty()) {
            return "unknown";
        }
        String maskedIp = maskIp(rawIp);
        return hashString(maskedIp);
    }

    // --- Maskierungs-Logik ---

    /**
     * Wählt die passende Maskierungsstrategie basierend auf dem erkannten IP-Typ.
     * <p>
     * Prüft mittels {@link #resolveIp(String)}, ob es sich technisch um eine v4
     * oder v6 Adresse handelt.
     * </p>
     *
     * @param ip Die zu maskierende IP-Adresse.
     * @return Die maskierte IP oder den gegebenen Parameter {@code "ip"}, wenn der
     *         String keine gültige IP ist.
     */
    private String maskIp(String ip) {
        if (isValidIPv6(ip)) {
            return maskIPv6(ip);
        } else if (isValidIPv4(ip)) {
            return maskIPv4(ip);
        }

        // Fallback für ungültige Formate, um Exceptions im Hash-Prozess zu vermeiden
        return ip;
    }

    /**
     * Maskiert eine <b>IPv4-Adresse,</b> indem das letzte Oktett auf 0 gesetzt
     * wird.
     * <br>
     * Beispiel: {@code 192.168.1.50} -> {@code 192.168.1.0}
     */
    private String maskIPv4(String ip) {
        // Letztes Oktett auf 0 setzen
        return ip.substring(0, ip.lastIndexOf('.')) + ".0";
    }

    /**
     * Maskiert eine <b>IPv6-Adresse.</b> Behält nur die ersten 3 Blöcke (ca. 48
     * Bit) bei.
     * <br>
     * Hinweis: Dies ist eine vereinfachte String-Implementierung für
     * Hashing-Zwecke.
     */
    private String maskIPv6(String ip) {
        // Wir benötigen mindestens 3 Teile (z.B. a:b:c:...), um das Präfix a:b:c zu
        // bilden.
        // Andernfalls geben wir eine komplett genullte Adresse zurück.
        String[] parts = ip.split(":");
        if (parts.length > 3) {
            return parts[0] + ":" + parts[1] + ":" + parts[2] + ":0:0:0:0:0";
        }
        return "0:0:0:0:0:0:0:0";
    }

    // --- Hashing ---

    /**
     * Erzeugt einen SHA-256 Hash aus dem Eingabe-String.
     * <p>
     * Der resultierende Hash wird in einen Hexadezimal-String umgewandelt und auf
     * die
     * ersten 8 Zeichen gekürzt.
     * </p>
     *
     * @param input Der zu hashende String (in der Regel die bereits maskierte
     *              IP-Adresse).
     * @return Die ersten 8 Zeichen des Hex-Hashes oder {@code "hash-error"}, falls
     *         der
     *         Algorithmus nicht verfügbar ist.
     */
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

    // --- Validierung ---

    /**
     * Prüft, ob der String technisch als IPv4-Adresse (Instanz von
     * {@link java.net.Inet4Address}) erkannt wird.
     */
    private boolean isValidIPv4(String ip) {
        InetAddress inetAddress = resolveIpAddress(ip);
        return inetAddress instanceof java.net.Inet4Address;
    }

    /**
     * Prüft, ob der String technisch als IPv6-Adresse (Instanz von
     * {@link java.net.Inet6Address}) erkannt wird.
     */
    private boolean isValidIPv6(String ip) {
        InetAddress inetAddress = resolveIpAddress(ip);
        return inetAddress instanceof java.net.Inet6Address;
    }

    /**
     * Versucht, den String in eine {@link InetAddress} umzuwandeln.
     *
     * @param ip Der IP-String.
     * @return Das {@link InetAddress} Objekt oder {@code null} bei Parse-Fehlern.
     */
    private InetAddress resolveIpAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
