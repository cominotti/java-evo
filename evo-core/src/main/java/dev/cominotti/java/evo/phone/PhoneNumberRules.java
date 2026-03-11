// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.phone;

/**
 * Validation rules and resource bundle keys for PhoneNumber.
 *
 * <p>Follows the ITU-T E.164 international telephone numbering format: a {@code "+"}
 * prefix followed by 1 to 15 digits. E.164 is the standard used by Twilio, AWS SNS,
 * Google Cloud, and virtually every telephony API for unambiguous international phone
 * number representation.</p>
 *
 * <p>The maximum length of 16 characters (1 for {@code "+"} plus up to 15 digits)
 * matches the E.164 specification exactly.</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.phoneNumber.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class PhoneNumberRules {

    private PhoneNumberRules() {}

    /** Regex: {@code "+"} followed by 1 to 15 digits (ITU-T E.164). */
    public static final String REGEX = "\\+\\d{1,15}";

    /** Maximum stored length: 1 for the {@code "+"} sign plus up to 15 digits. */
    public static final int MAX_LENGTH = 16;

    public static final String BLANK_MESSAGE = "{evo.phoneNumber.blank}";
    public static final String FORMAT_MESSAGE = "{evo.phoneNumber.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.phoneNumber.maxLength}";
    public static final String NULL_MESSAGE = "{evo.phoneNumber.null}";
}
