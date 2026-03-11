// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.phone;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Regional telephone area code.
 *
 * <p>A country-agnostic representation of the geographic routing prefix within a
 * country's numbering plan. Stored as 1–5 digits. Examples: {@code "11"} (São Paulo),
 * {@code "212"} (New York City), {@code "20"} (London), {@code "89"} (Munich).</p>
 *
 * <p>Designed to complement {@link PhoneNumber} in multi-field phone number storage:</p>
 * <pre>{@code
 * @Column(name = "area_code")
 * private AreaCode areaCode;       // "11"
 *
 * @Column(name = "phone")
 * private PhoneNumber phone;       // "+5511999998888"
 * }</pre>
 */
@EvoType
public record AreaCode(
        @NotBlank(message = AreaCodeRules.BLANK_MESSAGE)
        @Pattern(regexp = AreaCodeRules.REGEX, message = AreaCodeRules.FORMAT_MESSAGE)
        @Size(max = AreaCodeRules.MAX_LENGTH, message = AreaCodeRules.MAX_LENGTH_MESSAGE)
        String value
) {

    public AreaCode {
        EvoValidation.validate(AreaCode.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
