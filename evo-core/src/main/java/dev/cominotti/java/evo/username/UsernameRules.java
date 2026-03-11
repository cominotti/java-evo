// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.username;

/**
 * Validation rules and resource bundle keys for Username.
 *
 * <p>Follows the GitHub username convention: starts with a letter, contains only letters,
 * digits, hyphens, or underscores, and is between 3 and 39 characters long. This
 * convention is widely adopted across platforms (GitHub, GitLab, Bitbucket) and provides
 * a good balance between flexibility and safety.</p>
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.username.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class UsernameRules {

    private UsernameRules() {}

    /**
     * Regex: starts with a letter, followed by 2-38 alphanumeric, hyphen, or underscore
     * characters (total length 3-39). The leading-letter requirement prevents confusion
     * with numeric IDs and avoids issues with command-line tools that interpret leading
     * hyphens as flags.
     */
    public static final String REGEX = "[a-zA-Z][a-zA-Z0-9_-]{2,38}";

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 39;

    public static final String BLANK_MESSAGE = "{evo.username.blank}";
    public static final String FORMAT_MESSAGE = "{evo.username.format}";
    public static final String LENGTH_MESSAGE = "{evo.username.length}";
}
