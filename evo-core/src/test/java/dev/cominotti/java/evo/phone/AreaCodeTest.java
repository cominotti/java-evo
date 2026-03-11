// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.phone;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AreaCodeTest {

    @Test
    void validSingleDigitAreaCode() {
        var areaCode = new AreaCode("1");
        assertThat(areaCode.value()).isEqualTo("1");
    }

    @Test
    void validTwoDigitAreaCode() {
        var areaCode = new AreaCode("11");
        assertThat(areaCode.value()).isEqualTo("11");
    }

    @Test
    void validThreeDigitAreaCode() {
        var areaCode = new AreaCode("212");
        assertThat(areaCode.value()).isEqualTo("212");
    }

    @Test
    void validFiveDigitAreaCode() {
        var areaCode = new AreaCode("12345");
        assertThat(areaCode.value()).isEqualTo("12345");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new AreaCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new AreaCode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new AreaCode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void sixDigitsRejected() {
        assertThatThrownBy(() -> new AreaCode("123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 to 5 digits");
    }

    @Test
    void lettersRejected() {
        assertThatThrownBy(() -> new AreaCode("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 to 5 digits");
    }

    @Test
    void mixedAlphanumericRejected() {
        assertThatThrownBy(() -> new AreaCode("1a2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 to 5 digits");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new AreaCode("11");
        var b = new AreaCode("11");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new AreaCode("11");
        var b = new AreaCode("212");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var areaCode = new AreaCode("11");
        assertThat(areaCode).hasToString("11");
    }
}
