// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.slug;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * URL-friendly identifier (slug).
 *
 * <p>Slugs are lowercase alphanumeric strings with hyphens as word separators — the
 * universal format for human-readable URL segments. Examples: {@code "my-awesome-post"},
 * {@code "product-42"}, {@code "hello"}.</p>
 *
 * <p>Validation rejects uppercase letters, underscores, spaces, leading/trailing hyphens,
 * and consecutive hyphens. This mirrors the slug conventions of WordPress, Django, and
 * GitHub repository names.</p>
 */
@EvoType
public record Slug(
        @NotBlank(message = SlugRules.BLANK_MESSAGE)
        @Pattern(regexp = SlugRules.REGEX, message = SlugRules.FORMAT_MESSAGE)
        @Size(max = SlugRules.MAX_LENGTH, message = SlugRules.MAX_LENGTH_MESSAGE)
        String value
) {

    public Slug {
        EvoValidation.validate(Slug.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
