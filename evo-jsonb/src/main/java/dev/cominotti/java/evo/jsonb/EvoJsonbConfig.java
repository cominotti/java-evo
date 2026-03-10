// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ServiceLoader;

import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * Helper for registering EVO flat-string adapters with a {@link JsonbConfig}.
 *
 * <h3>Usage</h3>
 *
 * <p><b>Library EVOs only</b> (zero-config — adapters discovered via ServiceLoader):</p>
 * <pre>{@code
 * var jsonb = JsonbBuilder.create(
 *         EvoJsonbConfig.withDefaults(new JsonbConfig()));
 * }</pre>
 *
 * <p><b>Library + custom EVOs</b> (custom adapters also discovered via ServiceLoader):</p>
 * <pre>{@code
 * // 1. Create the adapter:
 * public class PhoneJsonbAdapter extends StringEvoJsonbAdapter<Phone> {
 *     public PhoneJsonbAdapter() { super(Phone::value, Phone.class); }
 * }
 *
 * // 2. Register in META-INF/services/dev.cominotti.java.evo.jsonb.EvoJsonbAdapterProvider:
 * //    com.myapp.PhoneJsonbAdapter
 *
 * // 3. withDefaults() auto-discovers it — no explicit registration:
 * var jsonb = JsonbBuilder.create(
 *         EvoJsonbConfig.withDefaults(new JsonbConfig()));
 * }</pre>
 *
 * <p>Custom adapters can also be passed explicitly (e.g., for test-only adapters):</p>
 * <pre>{@code
 * var jsonb = JsonbBuilder.create(
 *         EvoJsonbConfig.withDefaults(new JsonbConfig(),
 *                 new TestOnlyEvoAdapter()));
 * }</pre>
 *
 * <h3>Auto-discovery via ServiceLoader</h3>
 *
 * <p>{@link #withDefaults} scans for all {@link EvoJsonbAdapterProvider} implementations
 * registered via {@code META-INF/services}. Library-provided adapters ({@code Email},
 * {@code Cpf}, {@code Cnpj}, {@code CpfOrCnpj}) are pre-registered in the
 * {@code evo-jsonb} module's service file. Consumer adapters are discovered from
 * their own service files.</p>
 *
 * @see StringEvoJsonbAdapter
 * @see EvoJsonbAdapterProvider
 */
public final class EvoJsonbConfig {

    private EvoJsonbConfig() {}

    /**
     * Discovers and registers all EVO adapters found via {@link ServiceLoader},
     * plus any additional adapters passed explicitly.
     *
     * <p>Discovery scans for {@link EvoJsonbAdapterProvider} implementations in
     * {@code META-INF/services}. The library's service file registers adapters for
     * {@code Email}, {@code Cpf}, {@code Cnpj}, and {@code CpfOrCnpj}. Consumer
     * modules add their own adapters to the same service file.</p>
     *
     * @param config             the JSON-B config to augment (returned for fluent chaining)
     * @param additionalAdapters extra adapters not registered via ServiceLoader
     *                           (e.g., test-only adapters)
     * @return the same {@code config} instance with all discovered EVO adapters registered
     */
    public static JsonbConfig withDefaults(JsonbConfig config,
                                           JsonbAdapter<?, ?>... additionalAdapters) {
        var adapters = new ArrayList<JsonbAdapter<?, ?>>();

        // Auto-discover adapters registered via META-INF/services
        ServiceLoader.load(EvoJsonbAdapterProvider.class).forEach(provider -> {
            if (provider instanceof JsonbAdapter<?, ?> adapter) {
                adapters.add(adapter);
            }
        });

        // Add any explicitly passed adapters (test-only, unregistered, etc.)
        Collections.addAll(adapters, additionalAdapters);

        config.withAdapters(adapters.toArray(new JsonbAdapter[0]));
        return config;
    }
}
