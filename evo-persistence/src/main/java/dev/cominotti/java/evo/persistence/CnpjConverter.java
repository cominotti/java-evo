// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.taxid.Cnpj;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link Cnpj} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class CnpjConverter extends StringEvoConverter<Cnpj> {

    public CnpjConverter() {
        super(Cnpj::value, Cnpj::new);
    }
}
