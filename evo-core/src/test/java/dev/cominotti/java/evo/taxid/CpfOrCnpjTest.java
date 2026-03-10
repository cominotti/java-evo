package dev.cominotti.java.evo.taxid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfOrCnpjTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    @Test
    void ofWithElevenDigitsReturnsCpf() {
        CpfOrCnpj result = CpfOrCnpj.of(VALID_CPF);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void ofWithFourteenDigitsReturnsCnpj() {
        CpfOrCnpj result = CpfOrCnpj.of(VALID_CNPJ);
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void ofWithNullThrowsIllegalArgument() {
        assertThatThrownBy(() -> CpfOrCnpj.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void ofWithBlankThrowsIllegalArgument() {
        assertThatThrownBy(() -> CpfOrCnpj.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void ofWithWrongLengthThrowsIllegalArgument() {
        assertThatThrownBy(() -> CpfOrCnpj.of("1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("11 digits (CPF) or 14 digits (CNPJ)");
    }

    @Test
    void ofWithTwelveDigitsThrowsIllegalArgument() {
        assertThatThrownBy(() -> CpfOrCnpj.of("123456789012"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("11 digits (CPF) or 14 digits (CNPJ)");
    }

    @Test
    void ofWithInvalidCpfCheckDigitsPropagatesError() {
        assertThatThrownBy(() -> CpfOrCnpj.of("12345678901"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }

    @Test
    void ofWithInvalidCnpjCheckDigitsPropagatesError() {
        assertThatThrownBy(() -> CpfOrCnpj.of("12345678901234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }

    @Test
    void patternMatchingIsExhaustive() {
        CpfOrCnpj cpf = CpfOrCnpj.of(VALID_CPF);
        CpfOrCnpj cnpj = CpfOrCnpj.of(VALID_CNPJ);

        String cpfResult = switch (cpf) {
            case Cpf c -> "CPF:" + c.value();
            case Cnpj c -> "CNPJ:" + c.value();
        };

        String cnpjResult = switch (cnpj) {
            case Cpf c -> "CPF:" + c.value();
            case Cnpj c -> "CNPJ:" + c.value();
        };

        assertThat(cpfResult).isEqualTo("CPF:" + VALID_CPF);
        assertThat(cnpjResult).isEqualTo("CNPJ:" + VALID_CNPJ);
    }

    @Test
    void valueReturnsUnderlyingStringForBothTypes() {
        assertThat(CpfOrCnpj.of(VALID_CPF).value()).isEqualTo(VALID_CPF);
        assertThat(CpfOrCnpj.of(VALID_CNPJ).value()).isEqualTo(VALID_CNPJ);
    }

    // --- parse() tests ---

    @Test
    void parseFormattedCpf() {
        CpfOrCnpj result = CpfOrCnpj.parse("529.982.247-25");
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void parseFormattedCnpj() {
        CpfOrCnpj result = CpfOrCnpj.parse("11.222.333/0001-81");
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void parseNullThrowsIllegalArgument() {
        assertThatThrownBy(() -> CpfOrCnpj.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void parseWrongLengthAfterStripping() {
        assertThatThrownBy(() -> CpfOrCnpj.parse("123.456"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
