// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.greeting;

import dev.cominotti.java.evo.email.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GreetingRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 500) String message,
        Email email,
        ContactInfo contact
) {
}
