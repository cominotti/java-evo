// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.net;

import java.net.InetAddress;

/**
 * Validation rules and resource bundle keys for IpAddress.
 *
 * <p>Uses {@link InetAddress#ofLiteral(String)} (Java 22+) for validation — this method
 * parses IP address literals <em>without DNS resolution</em>, making it safe and fast
 * for validation. It accepts both IPv4 ({@code "192.168.1.1"}) and IPv6
 * ({@code "::1"}, {@code "2001:db8::1"}) formats.</p>
 *
 * <p>The maximum length of 45 characters accommodates the longest standard IPv6
 * representation (39 characters for a full 8-group form like
 * {@code "2001:0db8:85a3:0000:0000:8a2e:0370:7334"}) plus IPv4-mapped IPv6 forms
 * ({@code "::ffff:192.168.1.1"}).</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.ipAddress.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class IpAddressRules {

    private IpAddressRules() {}

    /** Maximum text length for an IP address (full IPv6 form is 39 chars, with margin for mapped forms). */
    public static final int MAX_LENGTH = 45;

    public static final String BLANK_MESSAGE = "{evo.ipAddress.blank}";
    public static final String FORMAT_MESSAGE = "{evo.ipAddress.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.ipAddress.maxLength}";

    /**
     * Validates an IP address literal using {@link InetAddress#ofLiteral(String)}.
     *
     * <p>This method performs no DNS resolution — it only parses the string
     * representation of IPv4 and IPv6 addresses. Throws {@link IllegalArgumentException}
     * for invalid literals (not {@code null} return), so we use a try-catch.</p>
     *
     * @param value the IP address literal to validate
     * @return {@code true} if the value is a valid IPv4 or IPv6 address literal
     */
    public static boolean isValidLiteral(String value) {
        try {
            InetAddress.ofLiteral(value);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}
