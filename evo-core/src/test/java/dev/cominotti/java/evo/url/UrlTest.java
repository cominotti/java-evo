// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.url;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlTest {

    @Test
    void validHttpsUrlCreatesSuccessfully() {
        var url = new Url("https://example.com");
        assertThat(url.value()).isEqualTo("https://example.com");
    }

    @Test
    void httpUrlAccepted() {
        var url = new Url("http://example.com");
        assertThat(url.value()).isEqualTo("http://example.com");
    }

    @Test
    void urlWithPathAccepted() {
        var url = new Url("https://example.com/path/to/resource");
        assertThat(url.value()).isEqualTo("https://example.com/path/to/resource");
    }

    @Test
    void urlWithQueryStringAccepted() {
        var url = new Url("https://example.com?key=value&other=123");
        assertThat(url.value()).isEqualTo("https://example.com?key=value&other=123");
    }

    @Test
    void urlWithFragmentAccepted() {
        var url = new Url("https://example.com#section");
        assertThat(url.value()).isEqualTo("https://example.com#section");
    }

    @Test
    void urlWithPortAccepted() {
        var url = new Url("https://example.com:8080");
        assertThat(url.value()).isEqualTo("https://example.com:8080");
    }

    @Test
    void ftpUrlAccepted() {
        var url = new Url("ftp://files.example.com");
        assertThat(url.value()).isEqualTo("ftp://files.example.com");
    }

    @Test
    void mailtoUrlAccepted() {
        var url = new Url("mailto:user@example.com");
        assertThat(url.value()).isEqualTo("mailto:user@example.com");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Url(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Url(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Url("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void noSchemeRejected() {
        assertThatThrownBy(() -> new Url("example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid URI with a scheme");
    }

    @Test
    void invalidUriSyntaxRejected() {
        assertThatThrownBy(() -> new Url("ht tp://bad url"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid URI with a scheme");
    }

    @Test
    void exceedingMaxLengthThrowsIllegalArgument() {
        var tooLong = "https://" + "a".repeat(2076);
        assertThatThrownBy(() -> new Url(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2083");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new Url("https://example.com");
        var b = new Url("https://example.com");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new Url("https://example.com");
        var b = new Url("https://other.com");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var url = new Url("https://example.com");
        assertThat(url).hasToString("https://example.com");
    }
}
