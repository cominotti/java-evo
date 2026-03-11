// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.url.Url;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link Url} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class UrlConverter extends StringEvoConverter<Url> {

    public UrlConverter() {
        super(Url::value, Url::new);
    }
}
