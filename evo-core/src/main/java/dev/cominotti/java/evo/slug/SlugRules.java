// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.slug;

/**
 * Validation rules and resource bundle keys for Slug.
 *
 * <p>A slug is a URL-friendly identifier composed of lowercase alphanumeric characters
 * and hyphens. No leading, trailing, or consecutive hyphens are allowed. This format
 * is the de facto standard for SEO-friendly URLs, used by WordPress, Django, GitHub,
 * and virtually every CMS and e-commerce platform.</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.slug.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class SlugRules {

    private SlugRules() {}

    /**
     * Regex: one or more lowercase-alphanumeric segments separated by single hyphens.
     * Rejects leading/trailing/consecutive hyphens and uppercase letters.
     */
    public static final String REGEX = "[a-z0-9]+(?:-[a-z0-9]+)*";

    public static final int MAX_LENGTH = 255;

    public static final String BLANK_MESSAGE = "{evo.slug.blank}";
    public static final String FORMAT_MESSAGE = "{evo.slug.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.slug.maxLength}";
}
