package dev.cominotti.java.evo.validation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Resolves EVO validation messages from the {@code ValidationMessages} resource bundle.
 *
 * <p>This utility serves the "direct throw" paths — {@code parse()} and {@code CpfOrCnpj.of()} —
 * which emit {@link IllegalArgumentException} outside Jakarta Validation's {@code MessageInterpolator}.
 * It reads from the same {@code ValidationMessages.properties} bundle that Jakarta Validation uses
 * for annotation-based messages, ensuring a single source of truth for all translations.</p>
 *
 * <h3>Key format</h3>
 *
 * <p>Accepts keys in Jakarta Validation interpolation format ({@code "{evo.cpf.blank}"}) or plain
 * format ({@code "evo.cpf.blank"}). Braces are stripped automatically before bundle lookup.</p>
 *
 * <h3>Parameterized messages</h3>
 *
 * <p>Supports {@link MessageFormat} parameters ({@code {0}}, {@code {1}}, etc.) for messages
 * like {@code "Tax ID must be ... got {0}"}. Pass parameter values as varargs to
 * {@link #resolve(String, Object...)}.</p>
 */
public final class EvoMessages {

    private static final String BUNDLE_NAME = "ValidationMessages";

    private EvoMessages() {}

    /**
     * Resolves a message key from the {@code ValidationMessages} resource bundle.
     *
     * <p>If the key is not found, returns the key itself as a fallback — this ensures
     * that missing translations produce a visible (if ugly) message rather than an exception.</p>
     *
     * @param messageOrKey a resource bundle key, optionally wrapped in Jakarta Validation
     *                     interpolation braces (e.g., {@code "{evo.cpf.blank}"} or {@code "evo.cpf.blank"})
     * @param args         optional {@link MessageFormat} parameters for parameterized messages
     * @return the resolved and formatted message, or the key itself if not found
     */
    public static String resolve(String messageOrKey, Object... args) {
        String key = messageOrKey;
        if (key.startsWith("{") && key.endsWith("}")) {
            key = key.substring(1, key.length() - 1);
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
            String template = bundle.getString(key);
            return args.length > 0 ? MessageFormat.format(template, args) : template;
        } catch (MissingResourceException e) {
            return args.length > 0 ? MessageFormat.format(messageOrKey, args) : messageOrKey;
        }
    }
}
