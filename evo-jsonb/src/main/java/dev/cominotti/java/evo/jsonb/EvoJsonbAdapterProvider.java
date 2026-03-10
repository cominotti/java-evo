// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

/**
 * Marker interface for EVO JSON-B adapters discoverable via {@link java.util.ServiceLoader}.
 *
 * <p>Implementations are auto-discovered by {@link EvoJsonbConfig#withDefaults(jakarta.json.bind.JsonbConfig,
 * jakarta.json.bind.adapter.JsonbAdapter[])} at startup — no explicit registration needed.</p>
 *
 * <p>All {@link StringEvoJsonbAdapter} subclasses inherit this interface automatically.
 * For non-record EVOs (like {@code CpfOrCnpj}), implement this interface directly.</p>
 *
 * <h3>Consumer usage</h3>
 *
 * <p>1. Create the adapter (one-liner):</p>
 * <pre>{@code
 * public class PhoneJsonbAdapter extends StringEvoJsonbAdapter<Phone> {
 *     public PhoneJsonbAdapter() { super(Phone::value, Phone.class); }
 * }
 * }</pre>
 *
 * <p>2. Register via {@code META-INF/services/dev.cominotti.java.evo.jsonb.EvoJsonbAdapterProvider}:</p>
 * <pre>
 * com.myapp.PhoneJsonbAdapter
 * </pre>
 *
 * <p>3. The adapter is auto-discovered by {@code EvoJsonbConfig.withDefaults()} — no
 * manual registration in code.</p>
 */
public interface EvoJsonbAdapterProvider {
}
