// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.country;

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
 * Validates that a string is a recognized ISO 3166-1 alpha-2 country code.
 *
 * <p>The validator defers null/blank handling to {@code @NotBlank} and format
 * checking to {@code @Pattern} — it only fires when the value is a non-null
 * two-uppercase-letter string that passes the format constraint. This avoids
 * duplicate error messages during multi-constraint evaluation.</p>
 *
 * <p>Lookup is performed against the JDK's {@link java.util.Locale#getISOCountries()}
 * list (~249 codes), cached in {@link CountryCodeRules} for O(1) access.</p>
 */
@Documented
@Constraint(validatedBy = ValidCountryCode.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {

    String message() default CountryCodeRules.INVALID_CODE_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Validates the country code against the ISO 3166-1 alpha-2 set.
     * Guards: defers to {@code @NotBlank} for null/blank, to {@code @Pattern} for format.
     */
    class Validator implements ConstraintValidator<ValidCountryCode, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // Defer null/blank to @NotBlank, non-matching format to @Pattern
            if (value == null || !value.matches(CountryCodeRules.REGEX)) {
                return true;
            }
            return CountryCodeRules.isValidIsoCountry(value);
        }
    }
}
