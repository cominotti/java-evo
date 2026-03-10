// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jakarta.example;

import dev.cominotti.java.evo.jsonb.EvoJsonbConfig;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Jakarta REST {@link ContextResolver} that provides a {@link Jsonb} instance
 * configured with EVO flat-string adapters.
 *
 * <p>Jersey's {@code JsonBindingFeature} detects this provider and uses the
 * returned {@link Jsonb} for all serialization/deserialization. Without this,
 * EVOs serialize as objects ({@code {"value":"..."}}) instead of flat strings.</p>
 *
 * <p>Must be a concrete class (not a lambda or anonymous class) so Jersey can
 * resolve the generic type parameter {@code Jsonb} from
 * {@code ContextResolver<Jsonb>} via reflection.</p>
 */
@Provider
public class EvoJsonbContextResolver implements ContextResolver<Jsonb> {

    private final Jsonb jsonb;

    public EvoJsonbContextResolver() {
        this.jsonb = JsonbBuilder.create(EvoJsonbConfig.withDefaults(new JsonbConfig()));
    }

    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }
}
