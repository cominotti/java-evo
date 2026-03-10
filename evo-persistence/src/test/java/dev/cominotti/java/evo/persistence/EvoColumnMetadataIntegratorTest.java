package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.Cpf;
import dev.cominotti.java.evo.CpfOrCnpj;
import dev.cominotti.java.evo.Email;
import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EmailRules;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EvoColumnMetadataIntegrator}'s internal utility methods.
 *
 * <p>Tests the two pure-reflection helpers directly:
 * {@code deriveLengthFromSizeAnnotation()} and {@code findDeclaredFieldType()}.
 * These run without a Hibernate or Spring context.</p>
 */
class EvoColumnMetadataIntegratorTest {

    // -- deriveLengthFromSizeAnnotation tests --

    @Test
    void derivesLengthFromSizeMaxAnnotation() {
        // Email has @Size(max = 320) on its "value" field.
        var length = EvoColumnMetadataIntegrator.deriveLengthFromSizeAnnotation(Email.class);
        assertThat(length).isEqualTo(EmailRules.MAX_LENGTH);
    }

    @Test
    void returnsNegativeOneWhenNoSizeAnnotation() {
        // Cpf uses @Pattern, not @Size — no max length derivable.
        var length = EvoColumnMetadataIntegrator.deriveLengthFromSizeAnnotation(Cpf.class);
        assertThat(length).isEqualTo(-1);
    }

    @Test
    void returnsNegativeOneForSealedInterface() {
        // CpfOrCnpj is a sealed interface, not a record — has no "value" field.
        var length = EvoColumnMetadataIntegrator.deriveLengthFromSizeAnnotation(CpfOrCnpj.class);
        assertThat(length).isEqualTo(-1);
    }

    @Test
    void ignoresSizeWithoutExplicitMax() {
        // @Size without max defaults to Integer.MAX_VALUE — the integrator must
        // NOT use this as a column length (would produce VARCHAR(2147483647)).
        var length = EvoColumnMetadataIntegrator.deriveLengthFromSizeAnnotation(
                SizeWithoutMax.class);
        assertThat(length).isEqualTo(-1);
    }

    // -- findDeclaredFieldType tests --

    @Test
    void findsFieldDeclaredDirectlyOnClass() {
        var type = EvoColumnMetadataIntegrator.findDeclaredFieldType(Child.class, "childField");
        assertThat(type).isEqualTo(String.class);
    }

    @Test
    void findsFieldDeclaredOnSuperclass() {
        // Simulates @MappedSuperclass inheritance — the field is on the parent class,
        // but the integrator receives the child class from Hibernate.
        var type = EvoColumnMetadataIntegrator.findDeclaredFieldType(Child.class, "parentEmail");
        assertThat(type).isEqualTo(Email.class);
    }

    @Test
    void returnsNullForNonexistentField() {
        var type = EvoColumnMetadataIntegrator.findDeclaredFieldType(Child.class, "doesNotExist");
        assertThat(type).isNull();
    }

    // -- Test fixtures --

    /**
     * EVO record with {@code @Size} but no explicit {@code max} —
     * {@code @Size.max()} defaults to {@code Integer.MAX_VALUE}.
     */
    @EvoType
    record SizeWithoutMax(
            @NotBlank
            @Size(min = 1) // no max specified — defaults to Integer.MAX_VALUE
            String value
    ) {
        SizeWithoutMax {
            EvoValidation.validate(SizeWithoutMax.class, "value", value);
        }
    }

    /** Simulates a {@code @MappedSuperclass} with an EVO field. */
    static class Parent {
        @SuppressWarnings("unused")
        private Email parentEmail;
    }

    /** Concrete entity class extending the "mapped superclass". */
    static class Child extends Parent {
        @SuppressWarnings("unused")
        private String childField;
    }
}
