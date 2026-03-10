package dev.cominotti.java.evo.email;

/**
 * Validation rules and resource bundle keys for Email.
 *
 * <p>Message constants are Jakarta Validation interpolation keys ({@code "{evo.email.blank}"})
 * resolved from {@code ValidationMessages.properties}.</p>
 */
public final class EmailRules {

    private EmailRules() {}

    public static final int MAX_LENGTH = 320;

    public static final String BLANK_MESSAGE = "{evo.email.blank}";
    public static final String FORMAT_MESSAGE = "{evo.email.format}";
    public static final String MAX_LENGTH_MESSAGE = "{evo.email.maxLength}";
}
