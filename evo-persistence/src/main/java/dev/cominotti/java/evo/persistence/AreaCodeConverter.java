// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.phone.AreaCode;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link AreaCode} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class AreaCodeConverter extends StringEvoConverter<AreaCode> {

    public AreaCodeConverter() {
        super(AreaCode::value, AreaCode::new);
    }
}
