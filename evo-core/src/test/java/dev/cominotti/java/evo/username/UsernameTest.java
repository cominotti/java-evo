// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.username;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsernameTest {

    @Test
    void validUsernameCreatesSuccessfully() {
        var username = new Username("alice-bob");
        assertThat(username.value()).isEqualTo("alice-bob");
    }

    @Test
    void threeCharUsernameAccepted() {
        var username = new Username("abc");
        assertThat(username.value()).isEqualTo("abc");
    }

    @Test
    void thirtyNineCharUsernameAccepted() {
        var username = new Username("a" + "b".repeat(38));
        assertThat(username.value()).hasSize(39);
    }

    @Test
    void usernameWithUnderscoreAccepted() {
        var username = new Username("alice_bob");
        assertThat(username.value()).isEqualTo("alice_bob");
    }

    @Test
    void usernameWithDigitsAccepted() {
        var username = new Username("user42");
        assertThat(username.value()).isEqualTo("user42");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Username(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Username(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void twoCharUsernameRejected() {
        assertThatThrownBy(() -> new Username("ab"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3");
    }

    @Test
    void fortyCharUsernameRejected() {
        assertThatThrownBy(() -> new Username("a" + "b".repeat(39)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("39");
    }

    @Test
    void startingWithDigitRejected() {
        assertThatThrownBy(() -> new Username("1user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start with a letter");
    }

    @Test
    void startingWithHyphenRejected() {
        assertThatThrownBy(() -> new Username("-user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start with a letter");
    }

    @Test
    void containsSpaceRejected() {
        assertThatThrownBy(() -> new Username("user name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start with a letter");
    }

    @Test
    void containsDotRejected() {
        assertThatThrownBy(() -> new Username("user.name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start with a letter");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new Username("alice");
        var b = new Username("alice");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new Username("alice");
        var b = new Username("bob42");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var username = new Username("alice");
        assertThat(username).hasToString("alice");
    }
}
