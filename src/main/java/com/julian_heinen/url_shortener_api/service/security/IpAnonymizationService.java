package com.julian_heinen.url_shortener_api.service.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

/**
 * Service for privacy-compliant anonymization and pseudonymization of IP
 * addresses.
 * <p>
 * This service implements the "Privacy by Design" principle by masking IP
 * addresses
 * <b>before</b> the hashing process. This prevents brute-force attacks (Rainbow
 * Tables)
 * on the hash from restoring the original user IP (k-anonymity).
 * </p>
 *
 * <h3>Masking Standards used:</h3>
 * <ul>
 * <li><b>IPv4:</b> The last octet (8 bits) is set to 0.
 * <br>
 * Example: {@code 192.168.123.50} &rarr; {@code 192.168.123.0}</li>
 * <li><b>IPv6:</b> The last 80 bits are set to 0 (only the first 48 bits / 3
 * blocks remain).
 * <br>
 * This corresponds to the industry standard of Google Analytics and prevents
 * tracing back to individual connections (/64 prefixes).
 * <br>
 * Example: {@code 2001:db8:85a3:0:0:8a2e:370:7334} &rarr;
 * {@code 2001:db8:85a3:0:0:0:0:0}</li>
 * </ul>
 *
 * @see <a href=
 *      "https://support.google.com/analytics/answer/2763052?hl=de">Google
 *      Analytics IP Anonymization</a>
 */
@Component
public class IpAnonymizationService {

    /**
     * Masks the provided IP address and creates a hash from it.
     *
     * @param rawIp The raw IP address (can be null, empty, or invalid).
     * @return An 8-character hex hash of the masked IP or fallback values
     *         ({@code "unknown"}, {@code "hash-error"}).
     */
    public String anonymizeAndHash(String rawIp) {
        if (rawIp == null || rawIp.isEmpty()) {
            return "unknown";
        }
        String maskedIp = maskIp(rawIp);
        return hashString(maskedIp);
    }

    // --- Masking Logic ---

    /**
     * Selects the appropriate masking strategy based on the detected IP type.
     * <p>
     * Checks via {@link #resolveIpAddress(String)} whether it is technically a v4
     * or v6 address.
     * </p>
     *
     * @param ip The IP address to be masked.
     * @return The masked IP or the original parameter {@code "ip"} if the
     *         string is not a valid IP.
     */
    private String maskIp(String ip) {
        if (isValidIPv6(ip)) {
            return maskIPv6(ip);
        } else if (isValidIPv4(ip)) {
            return maskIPv4(ip);
        }

        // Fallback for invalid formats to avoid exceptions during hashing
        return ip;
    }

    /**
     * Masks an <b>IPv4 address</b> by setting the last octet to 0.
     * <br>
     * Example: {@code 192.168.1.50} -> {@code 192.168.1.0}
     */
    private String maskIPv4(String ip) {
        // Set last octet to 0
        return ip.substring(0, ip.lastIndexOf('.')) + ".0";
    }

    /**
     * Masks an <b>IPv6 address</b>. Retains only the first 3 blocks (approx. 48
     * bits).
     * <br>
     * Note: This is a simplified string implementation for hashing purposes.
     */
    private String maskIPv6(String ip) {
        // At least 3 parts are required (e.g., a:b:c:...) to form the prefix a:b:c.
        // Otherwise, return a completely zeroed address.
        String[] parts = ip.split(":");
        if (parts.length > 3) {
            return parts[0] + ":" + parts[1] + ":" + parts[2] + ":0:0:0:0:0";
        }
        return "0:0:0:0:0:0:0:0";
    }

    // --- Hashing ---

    /**
     * Generates a SHA-256 hash from the input string.
     * <p>
     * The resulting hash is converted to a hexadecimal string and truncated to the
     * first 8 characters.
     * </p>
     *
     * @param input The string to hash (usually the already masked IP address).
     * @return The first 8 characters of the hex hash or {@code "hash-error"} if
     *         the algorithm is not available.
     */
    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Return first 8 characters of the hash
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

    // --- Validation ---

    /**
     * Checks if the string is technically recognized as an IPv4 address
     * (Instance of {@link java.net.Inet4Address}).
     */
    private boolean isValidIPv4(String ip) {
        InetAddress inetAddress = resolveIpAddress(ip);
        return inetAddress instanceof java.net.Inet4Address;
    }

    /**
     * Checks if the string is technically recognized as an IPv6 address
     * (Instance of {@link java.net.Inet6Address}).
     */
    private boolean isValidIPv6(String ip) {
        InetAddress inetAddress = resolveIpAddress(ip);
        return inetAddress instanceof java.net.Inet6Address;
    }

    /**
     * Attempts to convert the string into an {@link InetAddress}.
     *
     * @param ip The IP string.
     * @return The {@link InetAddress} object or {@code null} if parsing fails.
     */
    private InetAddress resolveIpAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException _) {
            return null;
        }
    }
}
