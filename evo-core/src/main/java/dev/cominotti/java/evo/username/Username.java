// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.username;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * User identifier following the GitHub username convention.
 *
 * <p>A username must start with a letter and contain only letters, digits, hyphens, or
 * underscores. Length is 3–39 characters. Examples: {@code "alice"}, {@code "bob-42"},
 * {@code "dev_user"}.</p>
 *
 * <p>The leading-letter requirement prevents confusion with numeric IDs and avoids
 * issues with CLI tools that interpret leading hyphens as flags.</p>
 */
@EvoType
public record Username(
        @NotBlank(message = UsernameRules.BLANK_MESSAGE)
        @Size(min = UsernameRules.MIN_LENGTH, max = UsernameRules.MAX_LENGTH,
                message = UsernameRules.LENGTH_MESSAGE)
        @Pattern(regexp = UsernameRules.REGEX, message = UsernameRules.FORMAT_MESSAGE)
        String value
) {

    public Username {
        EvoValidation.validate(Username.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
