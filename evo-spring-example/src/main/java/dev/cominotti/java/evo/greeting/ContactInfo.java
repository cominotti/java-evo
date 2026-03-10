// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.greeting;

import dev.cominotti.java.evo.email.Email;

public record ContactInfo(
        Email workEmail,
        AddressInfo address
) {
}
