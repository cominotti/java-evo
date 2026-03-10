// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.taxid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CpfCheckDigit.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfCheckDigit {

    String message() default "{evo.cpf.checkDigit}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<CpfCheckDigit, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || !value.matches(CpfRules.REGEX)) {
                return true;
            }
            return CpfRules.hasValidCheckDigits(value);
        }
    }
}
