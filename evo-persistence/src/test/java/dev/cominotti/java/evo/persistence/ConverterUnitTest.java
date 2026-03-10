// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EVO converters — no Spring context required.
 *
 * <p>Each converter extends {@link StringEvoConverter} and must handle null in both
 * directions, and round-trip correctly for valid values. Invalid values are rejected
 * by the EVO compact constructor (which throws {@code IllegalArgumentException}),
 * so converter tests only cover valid inputs and null.</p>
 */
class ConverterUnitTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    // --- EmailConverter ---

    @Test
    void emailConverterToDatabase() {
        var converter = new EmailConverter();
        assertThat(converter.convertToDatabaseColumn(new Email(VALID_EMAIL))).isEqualTo(VALID_EMAIL);
    }

    @Test
    void emailConverterFromDatabase() {
        var converter = new EmailConverter();
        assertThat(converter.convertToEntityAttribute(VALID_EMAIL).value()).isEqualTo(VALID_EMAIL);
    }

    @Test
    void emailConverterNullBothDirections() {
        var converter = new EmailConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CpfConverter ---

    @Test
    void cpfConverterRoundTrip() {
        var converter = new CpfConverter();
        var cpf = new Cpf(VALID_CPF);
        var dbValue = converter.convertToDatabaseColumn(cpf);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfConverterNullBothDirections() {
        var converter = new CpfConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CnpjConverter ---

    @Test
    void cnpjConverterRoundTrip() {
        var converter = new CnpjConverter();
        var cnpj = new Cnpj(VALID_CNPJ);
        var dbValue = converter.convertToDatabaseColumn(cnpj);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void cnpjConverterNullBothDirections() {
        var converter = new CnpjConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CpfOrCnpjConverter ---

    @Test
    void cpfOrCnpjConverterDispatchesByCpfLength() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj result = converter.convertToEntityAttribute(VALID_CPF);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfOrCnpjConverterDispatchesByCnpjLength() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj result = converter.convertToEntityAttribute(VALID_CNPJ);
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void cpfOrCnpjConverterNullBothDirections() {
        var converter = new CpfOrCnpjConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void cpfOrCnpjConverterRoundTripWithCpf() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj original = new Cpf(VALID_CPF);
        var dbValue = converter.convertToDatabaseColumn(original);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isInstanceOf(Cpf.class);
        assertThat(restored.value()).isEqualTo(original.value());
    }

    @Test
    void cpfOrCnpjConverterRoundTripWithCnpj() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj original = new Cnpj(VALID_CNPJ);
        var dbValue = converter.convertToDatabaseColumn(original);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isInstanceOf(Cnpj.class);
        assertThat(restored.value()).isEqualTo(original.value());
    }
}
