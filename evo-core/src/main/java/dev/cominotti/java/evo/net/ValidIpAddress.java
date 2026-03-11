// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.net;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Validates that a string is a valid IPv4 or IPv6 address literal.
 *
 * <p>Uses {@link java.net.InetAddress#ofLiteral(String)} (Java 22+) which performs
 * <em>no DNS resolution</em> — it only parses IP address literals, rejecting
 * hostnames and other non-IP strings. This is critical for validation: using
 * {@code InetAddress.getByName()} would perform DNS lookups, introducing latency
 * and network dependency.</p>
 *
 * <p>The validator defers null/blank handling to {@code @NotBlank}.</p>
 */
@Documented
@Constraint(validatedBy = ValidIpAddress.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIpAddress {

    String message() default IpAddressRules.FORMAT_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Validates the IP address literal via {@link IpAddressRules#isValidLiteral(String)}.
     * Guard: defers to {@code @NotBlank} for null/blank values.
     */
    class Validator implements ConstraintValidator<ValidIpAddress, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isBlank()) {
                return true;
            }
            return IpAddressRules.isValidLiteral(value);
        }
    }
}
