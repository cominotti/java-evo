// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.slug.Slug;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link Slug} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class SlugConverter extends StringEvoConverter<Slug> {

    public SlugConverter() {
        super(Slug::value, Slug::new);
    }
}
