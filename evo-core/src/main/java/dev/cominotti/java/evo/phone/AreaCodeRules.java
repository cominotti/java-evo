// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.phone;

/**
 * Validation rules and resource bundle keys for AreaCode.
 *
 * <p>Represents a regional/local telephone area code — the geographic routing prefix
 * within a country's numbering plan. Examples: {@code "11"} (São Paulo), {@code "212"}
 * (New York City), {@code "20"} (London). Country-agnostic — no country-specific
 * validation is applied, only digit format and length.</p>
 *
 * <p>Area code length varies globally from 1 to 5 digits. The 5-digit upper bound
 * accommodates all known national numbering plans.</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.areaCode.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class AreaCodeRules {

    private AreaCodeRules() {}

    /** Regex: 1 to 5 digits. */
    public static final String REGEX = "\\d{1,5}";

    public static final int MAX_LENGTH = 5;

    public static final String BLANK_MESSAGE = "{evo.areaCode.blank}";
    public static final String FORMAT_MESSAGE = "{evo.areaCode.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.areaCode.maxLength}";
}
