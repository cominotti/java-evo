// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.rest;

import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvoParamConverterProviderTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    private final EvoParamConverterProvider provider = new EvoParamConverterProvider();

    @Test
    void returnsConverterForEmailType() {
        var converter = provider.getConverter(Email.class, null, null);
        assertThat(converter).isNotNull();
    }

    @Test
    void returnsConverterForCpfType() {
        var converter = provider.getConverter(Cpf.class, null, null);
        assertThat(converter).isNotNull();
    }

    @Test
    void returnsConverterForCpfOrCnpjType() {
        var converter = provider.getConverter(CpfOrCnpj.class, null, null);
        assertThat(converter).isNotNull();
    }

    @Test
    void returnsNullForNonEvoType() {
        var converter = provider.getConverter(String.class, null, null);
        assertThat(converter).isNull();
    }

    @Test
    void emailConverterParsesValidValue() {
        var converter = provider.getConverter(Email.class, null, null);
        var email = converter.fromString("user@example.com");
        assertThat(email).isInstanceOf(Email.class);
        assertThat(((Email) email).value()).isEqualTo("user@example.com");
    }

    @Test
    void emailConverterRejectsInvalidValue() {
        var converter = provider.getConverter(Email.class, null, null);
        assertThatThrownBy(() -> converter.fromString("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emailConverterReturnsNullForNullInput() {
        var converter = provider.getConverter(Email.class, null, null);
        assertThat(converter.fromString(null)).isNull();
    }

    @Test
    void emailConverterToStringReturnsValue() {
        var converter = provider.getConverter(Email.class, null, null);
        var result = converter.toString(new Email("user@example.com"));
        assertThat(result).isEqualTo("user@example.com");
    }

    @Test
    void cpfOrCnpjConverterDispatchesCpfByLength() {
        var converter = provider.getConverter(CpfOrCnpj.class, null, null);
        var result = converter.fromString(VALID_CPF);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(((CpfOrCnpj) result).value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfOrCnpjConverterDispatchesCnpjByLength() {
        var converter = provider.getConverter(CpfOrCnpj.class, null, null);
        var result = converter.fromString(VALID_CNPJ);
        assertThat(result).isInstanceOf(dev.cominotti.java.evo.taxid.Cnpj.class);
    }
}
