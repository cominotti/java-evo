// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.url;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Validates that a string is a syntactically valid URI with a scheme.
 *
 * <p>Uses {@link URI} for structural validation and requires a non-null scheme.
 * Accepts any scheme (http, https, ftp, mailto, etc.) — applications that need
 * stricter scheme restrictions can add additional constraints at the field level.</p>
 *
 * <p>The validator defers null/blank handling to {@code @NotBlank}. Uses the
 * {@link URI#URI(String)} constructor (checked {@link URISyntaxException}) rather
 * than {@link URI#create(String)} (unchecked {@link IllegalArgumentException})
 * for cleaner control flow in a validator.</p>
 */
@Documented
@Constraint(validatedBy = ValidUrl.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {

    String message() default UrlRules.FORMAT_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Validates the URL by parsing with {@link URI} and checking for a non-null scheme.
     * Guard: defers to {@code @NotBlank} for null/blank values.
     */
    class Validator implements ConstraintValidator<ValidUrl, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isBlank()) {
                return true;
            }
            try {
                var uri = new URI(value);
                return uri.getScheme() != null;
            } catch (URISyntaxException _) {
                return false;
            }
        }
    }
}
