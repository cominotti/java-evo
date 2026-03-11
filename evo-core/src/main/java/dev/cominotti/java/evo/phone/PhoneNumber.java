// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.phone;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoMessages;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * International phone number in E.164 format.
 *
 * <p>Stored as a {@code "+"} prefix followed by 1–15 digits, with no formatting
 * characters. Examples: {@code "+5511999998888"} (Brazil), {@code "+14155551234"}
 * (US), {@code "+442012345678"} (UK).</p>
 *
 * <p>Use {@link #parse(String)} to accept human-formatted input like
 * {@code "+55 (11) 99999-8888"} — it strips all non-digit characters while
 * preserving (or adding) the leading {@code "+"} prefix.</p>
 */
@EvoType
public record PhoneNumber(
        @NotBlank(message = PhoneNumberRules.BLANK_MESSAGE)
        @Pattern(regexp = PhoneNumberRules.REGEX, message = PhoneNumberRules.FORMAT_MESSAGE)
        @Size(max = PhoneNumberRules.MAX_LENGTH, message = PhoneNumberRules.MAX_LENGTH_MESSAGE)
        String value
) {

    public PhoneNumber {
        EvoValidation.validate(PhoneNumber.class, "value", value);
    }

    /**
     * Parses a formatted phone number into E.164 canonical form.
     *
     * <p>Strips all non-digit characters (spaces, dashes, parentheses, dots) while
     * preserving or prepending the {@code "+"} prefix. If the input does not start
     * with {@code "+"}, one is added automatically.</p>
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "+55 (11) 99999-8888"} → {@code "+5511999998888"}</li>
     *   <li>{@code "5511999998888"} → {@code "+5511999998888"}</li>
     *   <li>{@code "+1-555-123-4567"} → {@code "+15551234567"}</li>
     * </ul>
     *
     * @param formatted the phone number with optional formatting characters
     * @return a validated PhoneNumber in E.164 format
     * @throws IllegalArgumentException if formatted is null or the stripped result is invalid
     */
    public static PhoneNumber parse(String formatted) {
        if (formatted == null) {
            throw new IllegalArgumentException(EvoMessages.resolve(PhoneNumberRules.NULL_MESSAGE));
        }
        // Preserve leading '+', strip all non-digit characters from the rest
        var stripped = formatted.startsWith("+")
                ? "+" + formatted.substring(1).replaceAll("\\D", "")
                : "+" + formatted.replaceAll("\\D", "");
        return new PhoneNumber(stripped);
    }

    @Override
    public String toString() {
        return value;
    }
}
