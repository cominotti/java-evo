// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo;

import java.lang.annotation.Annotation;
import java.util.Set;

import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.CnpjCheckDigit;
import dev.cominotti.java.evo.taxid.CnpjRules;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfCheckDigit;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import dev.cominotti.java.evo.taxid.CpfRules;
import dev.cominotti.java.evo.taxid.NotAllSameDigit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates EVO types using a plain Jakarta Validation {@link Validator} — no Spring context
 * required. The validator is obtained from {@link Validation#buildDefaultValidatorFactory()},
 * which discovers Hibernate Validator on the test classpath.
 */
class EvoValidationTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // --- Direct EVO validation: valid instances produce no violations ---

    @Test
    void validEmailHasNoViolations() {
        var email = new Email("user@example.com");
        Set<ConstraintViolation<Email>> violations = validator.validate(email);
        assertThat(violations).isEmpty();
    }

    @Test
    void validCpfHasNoViolations() {
        var cpf = new Cpf(VALID_CPF);
        Set<ConstraintViolation<Cpf>> violations = validator.validate(cpf);
        assertThat(violations).isEmpty();
    }

    @Test
    void validCnpjHasNoViolations() {
        var cnpj = new Cnpj(VALID_CNPJ);
        Set<ConstraintViolation<Cnpj>> violations = validator.validate(cnpj);
        assertThat(violations).isEmpty();
    }

    // --- Annotation metadata verification ---
    // Since compact constructors prevent creating invalid EVOs, we verify
    // that the correct Jakarta Validation annotations are declared on record
    // components. This ensures the annotations will work in framework contexts
    // (e.g., Jackson deserialization + @Valid before constructor invocation).

    @Test
    void emailHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Email.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void emailHasEmailAnnotation() {
        assertThat(hasAnnotation(Email.class, "value", jakarta.validation.constraints.Email.class)).isTrue();
    }

    @Test
    void emailHasSizeAnnotation() {
        assertThat(hasAnnotation(Email.class, "value", Size.class)).isTrue();
    }

    @Test
    void cpfHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Cpf.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void cpfHasPatternAnnotation() {
        var pattern = getAnnotation(Cpf.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(CpfRules.REGEX);
    }

    @Test
    void cpfHasNotAllSameDigitAnnotation() {
        assertThat(hasAnnotation(Cpf.class, "value", NotAllSameDigit.class)).isTrue();
    }

    @Test
    void cpfHasCpfCheckDigitAnnotation() {
        assertThat(hasAnnotation(Cpf.class, "value", CpfCheckDigit.class)).isTrue();
    }

    @Test
    void cnpjHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Cnpj.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void cnpjHasPatternAnnotation() {
        var pattern = getAnnotation(Cnpj.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(CnpjRules.REGEX);
    }

    @Test
    void cnpjHasNotAllSameDigitAnnotation() {
        assertThat(hasAnnotation(Cnpj.class, "value", NotAllSameDigit.class)).isTrue();
    }

    @Test
    void cnpjHasCnpjCheckDigitAnnotation() {
        assertThat(hasAnnotation(Cnpj.class, "value", CnpjCheckDigit.class)).isTrue();
    }

    @Test
    void allEvoTypesAreMarkedAsEvoTypes() {
        assertThat(Email.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Cpf.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Cnpj.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(CpfOrCnpj.class.isAnnotationPresent(EvoType.class)).isTrue();
    }

    private static boolean hasAnnotation(Class<? extends Record> recordClass,
                                         String componentName,
                                         Class<? extends Annotation> annotationType) {
        return getAnnotation(recordClass, componentName, annotationType) != null;
    }

    private static <A extends Annotation> A getAnnotation(Class<? extends Record> recordClass,
                                                           String componentName,
                                                           Class<A> annotationType) {
        // Jakarta Validation annotations don't target RECORD_COMPONENT,
        // so they propagate to the field and constructor parameter instead.
        // Look up the annotation on the declared field.
        try {
            var field = recordClass.getDeclaredField(componentName);
            return field.getAnnotation(annotationType);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
