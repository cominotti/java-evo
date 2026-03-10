// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.email.Email;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link Email} ↔ {@code String}.
 *
 * <p>Construction via {@code Email::new} triggers the compact constructor,
 * which validates the value through {@code EvoValidation.validate()} —
 * so invalid strings from the database are rejected at read time.</p>
 */
@Converter(autoApply = true)
public class EmailConverter extends StringEvoConverter<Email> {

    public EmailConverter() {
        super(Email::value, Email::new);
    }
}
