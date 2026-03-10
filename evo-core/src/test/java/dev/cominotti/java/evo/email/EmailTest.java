// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void validEmailCreatesSuccessfully() {
        var email = new Email("user@example.com");
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void minimalValidEmailAccepted() {
        var email = new Email("a@b");
        assertThat(email.value()).isEqualTo("a@b");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Email("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void missingAtSignThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Email("userexample.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid email");
    }

    @Test
    void exceedingMaxLengthThrowsIllegalArgument() {
        var tooLong = "a".repeat(310) + "@example.com";
        assertThatThrownBy(() -> new Email(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("320");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new Email("user@example.com");
        var b = new Email("user@example.com");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new Email("alice@example.com");
        var b = new Email("bob@example.com");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var email = new Email("user@example.com");
        assertThat(email).hasToString("user@example.com");
    }
}
