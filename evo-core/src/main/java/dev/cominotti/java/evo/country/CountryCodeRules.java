// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.country;

import java.util.Locale;
import java.util.Set;

/**
 * Validation rules and resource bundle keys for CountryCode.
 *
 * <p>Validates against the ISO 3166-1 alpha-2 standard — the universally adopted
 * two-letter country code system used by the UN, IANA, postal services, and every
 * major API (Stripe, PayPal, AWS, Google Cloud).</p>
 *
 * <p>The ISO country code set is cached at class-load time from
 * {@link Locale#getISOCountries()}, which returns the JDK's CLDR-based list of
 * ~249 recognized codes. The set is immutable and safe for concurrent access.</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.countryCode.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class CountryCodeRules {

    private CountryCodeRules() {}

    /** Regex: exactly 2 uppercase ASCII letters. */
    public static final String REGEX = "[A-Z]{2}";

    public static final int MAX_LENGTH = 2;

    public static final String BLANK_MESSAGE = "{evo.countryCode.blank}";
    public static final String FORMAT_MESSAGE = "{evo.countryCode.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.countryCode.maxLength}";
    public static final String INVALID_CODE_MESSAGE = "{evo.countryCode.invalid}";

    /**
     * Cached ISO 3166-1 alpha-2 country code set for O(1) lookup.
     * Loaded once at class initialization from the JDK's CLDR data.
     */
    private static final Set<String> ISO_COUNTRY_CODES = Set.of(Locale.getISOCountries());

    /**
     * Checks whether the given code is a recognized ISO 3166-1 alpha-2 country code.
     *
     * @param code the two-letter country code to check (must already be uppercase)
     * @return {@code true} if the code is in the ISO 3166-1 alpha-2 list
     */
    public static boolean isValidIsoCountry(String code) {
        return ISO_COUNTRY_CODES.contains(code);
    }
}
