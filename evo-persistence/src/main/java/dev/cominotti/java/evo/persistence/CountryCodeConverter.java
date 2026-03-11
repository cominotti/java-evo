// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.country.CountryCode;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link CountryCode} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class CountryCodeConverter extends StringEvoConverter<CountryCode> {

    public CountryCodeConverter() {
        super(CountryCode::value, CountryCode::new);
    }
}
