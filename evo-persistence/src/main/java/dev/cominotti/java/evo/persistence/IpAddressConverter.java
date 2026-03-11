// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.net.IpAddress;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link IpAddress} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class IpAddressConverter extends StringEvoConverter<IpAddress> {

    public IpAddressConverter() {
        super(IpAddress::value, IpAddress::new);
    }
}
