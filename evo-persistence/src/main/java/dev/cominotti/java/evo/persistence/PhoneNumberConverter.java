// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.phone.PhoneNumber;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link PhoneNumber} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class PhoneNumberConverter extends StringEvoConverter<PhoneNumber> {

    public PhoneNumberConverter() {
        super(PhoneNumber::value, PhoneNumber::new);
    }
}
