package dev.cominotti.java.evo.jsonb;

import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * Helper for registering EVO flat-string adapters with a {@link JsonbConfig}.
 *
 * <h3>Usage</h3>
 *
 * <p><b>Library EVOs only</b> (zero-config for built-in types):</p>
 * <pre>{@code
 * var jsonb = JsonbBuilder.create(
 *         EvoJsonbConfig.withDefaults(new JsonbConfig()));
 * }</pre>
 *
 * <p><b>Library + custom EVOs:</b></p>
 * <pre>{@code
 * var jsonb = JsonbBuilder.create(
 *         EvoJsonbConfig.withDefaults(new JsonbConfig(),
 *                 new PhoneJsonbAdapter(), new CepJsonbAdapter()));
 * }</pre>
 *
 * <h3>Why per-type adapter classes are needed</h3>
 *
 * <p>JSON-B matches adapters by resolving their generic type parameter {@code T}
 * from class metadata. A generic adapter erases {@code T} to its bound, so JSON-B
 * cannot determine which type it handles. Concrete subclasses of
 * {@link StringEvoJsonbAdapter} (e.g., {@code EmailJsonbAdapter extends
 * StringEvoJsonbAdapter<Email>}) retain the type parameter in class metadata.</p>
 *
 * <p>Custom EVO adapters follow the same one-liner pattern:</p>
 * <pre>{@code
 * public class PhoneJsonbAdapter extends StringEvoJsonbAdapter<Phone> {
 *     public PhoneJsonbAdapter() { super(Phone::value, Phone.class); }
 * }
 * }</pre>
 *
 * @see StringEvoJsonbAdapter
 */
public final class EvoJsonbConfig {

    /** The library-provided adapters registered by {@link #withDefaults}. */
    private static final JsonbAdapter<?, ?>[] DEFAULT_ADAPTERS = {
            new EmailJsonbAdapter(),
            new CpfJsonbAdapter(),
            new CnpjJsonbAdapter(),
            new CpfOrCnpjJsonbAdapter()
    };

    private EvoJsonbConfig() {}

    /**
     * Registers adapters for all library-provided EVO types ({@code Email},
     * {@code Cpf}, {@code Cnpj}, {@code CpfOrCnpj}) plus any additional
     * custom EVO adapters.
     *
     * @param config             the JSON-B config to augment (returned for fluent chaining)
     * @param additionalAdapters custom EVO adapters to register alongside the defaults
     * @return the same {@code config} instance with EVO adapters registered
     */
    public static JsonbConfig withDefaults(JsonbConfig config,
                                           JsonbAdapter<?, ?>... additionalAdapters) {
        config.withAdapters(DEFAULT_ADAPTERS);
        if (additionalAdapters.length > 0) {
            config.withAdapters(additionalAdapters);
        }
        return config;
    }
}
