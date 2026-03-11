// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.url;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Web or API URL.
 *
 * <p>A syntactically valid URI with a scheme, suitable for storing web links, API
 * endpoints, webhook URLs, image references, and OAuth redirect URIs. Examples:
 * {@code "https://example.com"}, {@code "ftp://files.example.com"},
 * {@code "mailto:user@example.com"}.</p>
 *
 * <p>The {@link ValidUrl} constraint ensures the value is parseable by {@link java.net.URI}
 * and has a non-null scheme. Any URI scheme is accepted — applications that need
 * to restrict to specific schemes (e.g., http/https only) can add additional
 * constraints on entity or DTO fields.</p>
 */
@EvoType
public record Url(
        @NotBlank(message = UrlRules.BLANK_MESSAGE)
        @Size(max = UrlRules.MAX_LENGTH, message = UrlRules.MAX_LENGTH_MESSAGE)
        @ValidUrl(message = UrlRules.FORMAT_MESSAGE)
        String value
) {

    public Url {
        EvoValidation.validate(Url.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
