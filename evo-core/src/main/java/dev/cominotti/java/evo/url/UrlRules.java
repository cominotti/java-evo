// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.url;

/**
 * Validation rules and resource bundle keys for Url.
 *
 * <p>The maximum length of 2083 characters is the practical limit historically imposed
 * by Internet Explorer and widely adopted as a safe upper bound by web servers (Apache,
 * Nginx), CDNs, and HTTP clients. While RFC 2616 does not define a maximum URL length,
 * 2083 is the de facto standard.</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.url.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class UrlRules {

    private UrlRules() {}

    /** Practical maximum URL length (de facto standard from IE, adopted by most web servers). */
    public static final int MAX_LENGTH = 2083;

    public static final String BLANK_MESSAGE = "{evo.url.blank}";
    public static final String FORMAT_MESSAGE = "{evo.url.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.url.maxLength}";
}
