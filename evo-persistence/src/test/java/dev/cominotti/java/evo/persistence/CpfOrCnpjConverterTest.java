// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfOrCnpjConverterTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    private final CpfOrCnpjConverter converter = new CpfOrCnpjConverter();

    @Test
    void convertCpfToDatabaseColumn() {
        var cpf = new Cpf(VALID_CPF);
        assertThat(converter.convertToDatabaseColumn(cpf)).isEqualTo(VALID_CPF);
    }

    @Test
    void convertCnpjToDatabaseColumn() {
        var cnpj = new Cnpj(VALID_CNPJ);
        assertThat(converter.convertToDatabaseColumn(cnpj)).isEqualTo(VALID_CNPJ);
    }

    @Test
    void convertNullToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertDatabaseColumnToCpf() {
        CpfOrCnpj result = converter.convertToEntityAttribute(VALID_CPF);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void convertDatabaseColumnToCnpj() {
        CpfOrCnpj result = converter.convertToEntityAttribute(VALID_CNPJ);
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void convertNullDatabaseColumnToNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void cpfRoundTrip() {
        CpfOrCnpj original = new Cpf(VALID_CPF);
        String dbValue = converter.convertToDatabaseColumn(original);
        CpfOrCnpj restored = converter.convertToEntityAttribute(dbValue);

        assertThat(restored).isInstanceOf(Cpf.class);
        assertThat(restored.value()).isEqualTo(original.value());
    }

    @Test
    void cnpjRoundTrip() {
        CpfOrCnpj original = new Cnpj(VALID_CNPJ);
        String dbValue = converter.convertToDatabaseColumn(original);
        CpfOrCnpj restored = converter.convertToEntityAttribute(dbValue);

        assertThat(restored).isInstanceOf(Cnpj.class);
        assertThat(restored.value()).isEqualTo(original.value());
    }
}
