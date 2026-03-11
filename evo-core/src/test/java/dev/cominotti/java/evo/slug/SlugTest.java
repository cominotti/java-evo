// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.slug;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlugTest {

    @Test
    void validSlugCreatesSuccessfully() {
        var slug = new Slug("my-awesome-post");
        assertThat(slug.value()).isEqualTo("my-awesome-post");
    }

    @Test
    void singleWordSlugAccepted() {
        var slug = new Slug("hello");
        assertThat(slug.value()).isEqualTo("hello");
    }

    @Test
    void numericSlugAccepted() {
        var slug = new Slug("123");
        assertThat(slug.value()).isEqualTo("123");
    }

    @Test
    void mixedAlphanumericSlugAccepted() {
        var slug = new Slug("post-42-draft");
        assertThat(slug.value()).isEqualTo("post-42-draft");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Slug(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Slug(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Slug("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void uppercaseRejected() {
        assertThatThrownBy(() -> new Slug("My-Slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void leadingHyphenRejected() {
        assertThatThrownBy(() -> new Slug("-slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void trailingHyphenRejected() {
        assertThatThrownBy(() -> new Slug("slug-"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void consecutiveHyphensRejected() {
        assertThatThrownBy(() -> new Slug("slug--name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void underscoreRejected() {
        assertThatThrownBy(() -> new Slug("slug_name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void spaceRejected() {
        assertThatThrownBy(() -> new Slug("slug name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void exceedingMaxLengthThrowsIllegalArgument() {
        var tooLong = "a".repeat(256);
        assertThatThrownBy(() -> new Slug(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
    }

    @Test
    void equalityBasedOnValue() {
        var a = new Slug("my-slug");
        var b = new Slug("my-slug");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new Slug("slug-one");
        var b = new Slug("slug-two");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var slug = new Slug("my-slug");
        assertThat(slug).hasToString("my-slug");
    }
}
