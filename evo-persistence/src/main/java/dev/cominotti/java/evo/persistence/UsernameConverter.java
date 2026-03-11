// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.username.Username;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link Username} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class UsernameConverter extends StringEvoConverter<Username> {

    public UsernameConverter() {
        super(Username::value, Username::new);
    }
}
