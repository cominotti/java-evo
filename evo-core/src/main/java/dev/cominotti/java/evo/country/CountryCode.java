// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.country;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * ISO 3166-1 alpha-2 country code.
 *
 * <p>The two-letter country code standard used by the UN, IANA, postal services, and
 * every major API. Examples: {@code "BR"} (Brazil), {@code "US"} (United States),
 * {@code "DE"} (Germany), {@code "JP"} (Japan).</p>
 *
 * <p>Validation ensures the value is exactly two uppercase letters <em>and</em> a
 * recognized ISO 3166-1 alpha-2 code — {@code "XX"} passes the format check but
 * fails the {@link ValidCountryCode} lookup against the JDK's ISO country list.</p>
 */
@EvoType
public record CountryCode(
        @NotBlank(message = CountryCodeRules.BLANK_MESSAGE)
        @Pattern(regexp = CountryCodeRules.REGEX, message = CountryCodeRules.FORMAT_MESSAGE)
        @Size(max = CountryCodeRules.MAX_LENGTH, message = CountryCodeRules.MAX_LENGTH_MESSAGE)
        @ValidCountryCode(message = CountryCodeRules.INVALID_CODE_MESSAGE)
        String value
) {

    public CountryCode {
        EvoValidation.validate(CountryCode.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
