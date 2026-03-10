package dev.cominotti.java.evo.taxid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnpjTest {

    // Known valid CNPJs (verified against official algorithm)
    private static final String VALID_CNPJ_1 = "11222333000181";
    private static final String VALID_CNPJ_2 = "11444777000161";
    private static final String VALID_CNPJ_3 = "00123456000149";

    @Test
    void validCnpjCreatesSuccessfully() {
        var cnpj = new Cnpj(VALID_CNPJ_1);
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ_1);
    }

    @Test
    void secondValidCnpjConfirmsAlgorithm() {
        var cnpj = new Cnpj(VALID_CNPJ_2);
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ_2);
    }

    @Test
    void thirdValidCnpjWithLeadingZeros() {
        var cnpj = new Cnpj(VALID_CNPJ_3);
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ_3);
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Cnpj(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Cnpj(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void nonDigitCharactersThrowIllegalArgument() {
        assertThatThrownBy(() -> new Cnpj("11.222.333/0001-81"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14 digits");
    }

    @Test
    void tooFewDigitsThrowIllegalArgument() {
        assertThatThrownBy(() -> new Cnpj("1122233300018"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14 digits");
    }

    @Test
    void tooManyDigitsThrowIllegalArgument() {
        assertThatThrownBy(() -> new Cnpj("112223330001811"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14 digits");
    }

    @Test
    void allZerosRejected() {
        assertThatThrownBy(() -> new Cnpj("00000000000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identical");
    }

    @Test
    void allNinesRejected() {
        assertThatThrownBy(() -> new Cnpj("99999999999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identical");
    }

    @Test
    void allSameDigitRejectedForEachDigit() {
        for (int d = 0; d <= 9; d++) {
            String allSame = String.valueOf(d).repeat(14);
            assertThatThrownBy(() -> new Cnpj(allSame))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("identical");
        }
    }

    @Test
    void invalidCheckDigitRejected() {
        // Modify last digit of a valid CNPJ
        assertThatThrownBy(() -> new Cnpj("11222333000182"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }

    @Test
    void invalidFirstCheckDigitRejected() {
        // Modify 13th digit (first check digit) of a valid CNPJ
        assertThatThrownBy(() -> new Cnpj("11222333000191"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }

    @Test
    void implementsCpfOrCnpj() {
        CpfOrCnpj taxId = new Cnpj(VALID_CNPJ_1);
        assertThat(taxId).isInstanceOf(Cnpj.class);
        assertThat(taxId.value()).isEqualTo(VALID_CNPJ_1);
    }

    @Test
    void equalityBasedOnValue() {
        var a = new Cnpj(VALID_CNPJ_1);
        var b = new Cnpj(VALID_CNPJ_1);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new Cnpj(VALID_CNPJ_1);
        var b = new Cnpj(VALID_CNPJ_2);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var cnpj = new Cnpj(VALID_CNPJ_1);
        assertThat(cnpj.toString()).isEqualTo(VALID_CNPJ_1);
    }

    // --- parse() tests ---

    @Test
    void parseFormattedCnpj() {
        var cnpj = Cnpj.parse("11.222.333/0001-81");
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ_1);
    }

    @Test
    void parseUnformattedCnpj() {
        var cnpj = Cnpj.parse(VALID_CNPJ_1);
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ_1);
    }

    @Test
    void parseNullThrowsIllegalArgument() {
        assertThatThrownBy(() -> Cnpj.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void parseInvalidCheckDigitsAfterStripping() {
        assertThatThrownBy(() -> Cnpj.parse("11.222.333/0001-82"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check digits");
    }
}
