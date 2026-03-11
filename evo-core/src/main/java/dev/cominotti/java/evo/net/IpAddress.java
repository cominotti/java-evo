// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.net;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * IPv4 or IPv6 address.
 *
 * <p>Used for logging, security (allow/block lists), rate limiting, geolocation,
 * audit trails, and network configuration. Accepts both IPv4 ({@code "192.168.1.1"})
 * and IPv6 ({@code "::1"}, {@code "2001:db8::1"}, {@code "::ffff:192.168.1.1"})
 * formats.</p>
 *
 * <p>Validation uses {@link java.net.InetAddress#ofLiteral(String)} (Java 22+) which
 * parses IP address literals <em>without DNS resolution</em> — hostnames like
 * {@code "example.com"} are rejected.</p>
 */
@EvoType
public record IpAddress(
        @NotBlank(message = IpAddressRules.BLANK_MESSAGE)
        @Size(max = IpAddressRules.MAX_LENGTH, message = IpAddressRules.MAX_LENGTH_MESSAGE)
        @ValidIpAddress(message = IpAddressRules.FORMAT_MESSAGE)
        String value
) {

    public IpAddress {
        EvoValidation.validate(IpAddress.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
