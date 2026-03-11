// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.phone;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    private static final String VALID_BRAZIL = "+5511999887766";
    private static final String VALID_US = "+14155551234";

    @Test
    void validE164PhoneNumberCreatesSuccessfully() {
        var phone = new PhoneNumber(VALID_BRAZIL);
        assertThat(phone.value()).isEqualTo(VALID_BRAZIL);
    }

    @Test
    void minimumValidPhoneNumber() {
        var phone = new PhoneNumber("+1");
        assertThat(phone.value()).isEqualTo("+1");
    }

    @Test
    void maximumValidPhoneNumber() {
        var phone = new PhoneNumber("+123456789012345");
        assertThat(phone.value()).isEqualTo("+123456789012345");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new PhoneNumber(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new PhoneNumber("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void missingPlusRejected() {
        assertThatThrownBy(() -> new PhoneNumber("5511999887766"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E.164");
    }

    @Test
    void tooManyDigitsRejected() {
        assertThatThrownBy(() -> new PhoneNumber("+1234567890123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lettersRejected() {
        assertThatThrownBy(() -> new PhoneNumber("+55abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E.164");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new PhoneNumber(VALID_BRAZIL);
        var b = new PhoneNumber(VALID_BRAZIL);
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new PhoneNumber(VALID_BRAZIL);
        var b = new PhoneNumber(VALID_US);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var phone = new PhoneNumber(VALID_BRAZIL);
        assertThat(phone).hasToString(VALID_BRAZIL);
    }

    // --- parse() tests ---

    @Test
    void parseFormattedPhoneNumber() {
        var phone = PhoneNumber.parse("+55 (11) 99988-7766");
        assertThat(phone.value()).isEqualTo(VALID_BRAZIL);
    }

    @Test
    void parseWithoutLeadingPlus() {
        var phone = PhoneNumber.parse("5511999887766");
        assertThat(phone.value()).isEqualTo(VALID_BRAZIL);
    }

    @Test
    void parseWithSpacesAndDashes() {
        var phone = PhoneNumber.parse("+1-415-555-1234");
        assertThat(phone.value()).isEqualTo(VALID_US);
    }

    @Test
    void parseUnformattedPassesThrough() {
        var phone = PhoneNumber.parse(VALID_BRAZIL);
        assertThat(phone.value()).isEqualTo(VALID_BRAZIL);
    }

    @Test
    void parseNullThrowsIllegalArgument() {
        assertThatThrownBy(() -> PhoneNumber.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }
}
