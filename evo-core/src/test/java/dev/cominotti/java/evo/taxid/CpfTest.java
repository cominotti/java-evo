// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.taxid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    // Known valid CPFs (verified against official algorithm)
    private static final String VALID_CPF_1 = "52998224725";
    private static final String VALID_CPF_2 = "11144477735";
    private static final String VALID_CPF_3 = "00123456797";

    @Test
    void validCpfCreatesSuccessfully() {
        var cpf = new Cpf(VALID_CPF_1);
        assertThat(cpf.value()).isEqualTo(VALID_CPF_1);
    }

    @Test
    void secondValidCpfConfirmsAlgorithm() {
        var cpf = new Cpf(VALID_CPF_2);
        assertThat(cpf.value()).isEqualTo(VALID_CPF_2);
    }

    @Test
    void thirdValidCpfWithLeadingZeros() {
        var cpf = new Cpf(VALID_CPF_3);
        assertThat(cpf.value()).isEqualTo(VALID_CPF_3);
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Cpf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Cpf(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void nonDigitCharactersThrowIllegalArgument() {
        assertThatThrownBy(() -> new Cpf("529.982.247-25"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("11 digits");
    }

    @Test
    void tooFewDigitsThrowIllegalArgument() {
        assertThatThrownBy(() -> new Cpf("1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("11 digits");
    }

    @Test
    void tooManyDigitsThrowIllegalArgument() {
        assertThatThrownBy(() -> new Cpf("123456789012"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("11 digits");
    }

    @Test
    void allZerosRejected() {
        assertThatThrownBy(() -> new Cpf("00000000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identical");
    }

    @Test
    void allNinesRejected() {
        assertThatThrownBy(() -> new Cpf("99999999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identical");
    }

    @Test
    void allSameDigitRejectedForEachDigit() {
        for (int d = 0; d <= 9; d++) {
            String allSame = String.valueOf(d).repeat(11);
            assertThatThrownBy(() -> new Cpf(allSame))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("identical");
        }
    }

    @Test
    void invalidCheckDigitRejected() {
        // Modify last digit of a valid CPF
        assertThatThrownBy(() -> new Cpf("52998224726"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }

    @Test
    void invalidFirstCheckDigitRejected() {
        // Modify 10th digit (first check digit) of a valid CPF
        assertThatThrownBy(() -> new Cpf("52998224735"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }

    @Test
    void implementsCpfOrCnpj() {
        CpfOrCnpj taxId = new Cpf(VALID_CPF_1);
        assertThat(taxId).isInstanceOf(Cpf.class);
        assertThat(taxId.value()).isEqualTo(VALID_CPF_1);
    }

    @Test
    void equalityBasedOnValue() {
        var a = new Cpf(VALID_CPF_1);
        var b = new Cpf(VALID_CPF_1);
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new Cpf(VALID_CPF_1);
        var b = new Cpf(VALID_CPF_2);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var cpf = new Cpf(VALID_CPF_1);
        assertThat(cpf).hasToString(VALID_CPF_1);
    }

    // --- parse() tests ---

    @Test
    void parseFormattedCpf() {
        var cpf = Cpf.parse("529.982.247-25");
        assertThat(cpf.value()).isEqualTo(VALID_CPF_1);
    }

    @Test
    void parseUnformattedCpf() {
        var cpf = Cpf.parse(VALID_CPF_1);
        assertThat(cpf.value()).isEqualTo(VALID_CPF_1);
    }

    @Test
    void parseWithSpaces() {
        var cpf = Cpf.parse("529 982 247 25");
        assertThat(cpf.value()).isEqualTo(VALID_CPF_1);
    }

    @Test
    void parseNullThrowsIllegalArgument() {
        assertThatThrownBy(() -> Cpf.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void parseInvalidCheckDigitsAfterStripping() {
        assertThatThrownBy(() -> Cpf.parse("529.982.247-26"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }
}
