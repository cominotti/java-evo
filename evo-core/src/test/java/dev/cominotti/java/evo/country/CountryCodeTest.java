// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.country;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CountryCodeTest {

    @Test
    void validCountryCodeCreatesSuccessfully() {
        var code = new CountryCode("BR");
        assertThat(code.value()).isEqualTo("BR");
    }

    @Test
    void unitedStatesAccepted() {
        var code = new CountryCode("US");
        assertThat(code.value()).isEqualTo("US");
    }

    @Test
    void germanyAccepted() {
        var code = new CountryCode("DE");
        assertThat(code.value()).isEqualTo("DE");
    }

    @Test
    void japanAccepted() {
        var code = new CountryCode("JP");
        assertThat(code.value()).isEqualTo("JP");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new CountryCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new CountryCode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new CountryCode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void lowercaseRejected() {
        assertThatThrownBy(() -> new CountryCode("br"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 uppercase letters");
    }

    @Test
    void threeLettersRejected() {
        assertThatThrownBy(() -> new CountryCode("BRA"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void singleLetterRejected() {
        assertThatThrownBy(() -> new CountryCode("B"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void digitsRejected() {
        assertThatThrownBy(() -> new CountryCode("12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 uppercase letters");
    }

    @Test
    void invalidIsoCodeRejected() {
        // "XX" passes the format check but is not a recognized ISO 3166-1 alpha-2 code
        assertThatThrownBy(() -> new CountryCode("XX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISO 3166-1");
    }

    @Test
    void anotherInvalidIsoCodeRejected() {
        assertThatThrownBy(() -> new CountryCode("ZZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISO 3166-1");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new CountryCode("BR");
        var b = new CountryCode("BR");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new CountryCode("BR");
        var b = new CountryCode("US");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var code = new CountryCode("BR");
        assertThat(code).hasToString("BR");
    }
}
