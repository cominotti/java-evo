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
@Constraint(validatedBy = CnpjCheckDigit.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CnpjCheckDigit {

    String message() default "{evo.cnpj.checkDigit}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<CnpjCheckDigit, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || !value.matches(CnpjRules.REGEX)) {
                return true;
            }
            return CnpjRules.hasValidCheckDigits(value);
        }
    }
}
