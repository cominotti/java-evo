// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jackson;

import java.lang.reflect.RecordComponent;

import dev.cominotti.java.evo.EvoTypes;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson {@link ValueSerializerModifier} that serializes single-component EVO records
 * as flat JSON strings instead of objects.
 *
 * <p>Without this modifier, a record like {@code Email("user@example.com")} would serialize
 * as {@code {"value":"user@example.com"}}. With the modifier, it serializes as
 * {@code "user@example.com"} — a bare string.</p>
 *
 * <p>Detection predicate: {@link #isSingleStringEvoRecord(Class)} checks for
 * {@code @EvoType} (not {@code @Embeddable}, which was removed from EVOs
 * to decouple them from Jakarta Persistence).</p>
 */
class SingleValueEvoSerializer extends ValueSerializerModifier {

    @Override
    public ValueSerializer<?> modifySerializer(
            SerializationConfig config,
            BeanDescription.Supplier beanDesc,
            ValueSerializer<?> serializer) {

        Class<?> beanClass = beanDesc.getBeanClass();
        if (isSingleStringEvoRecord(beanClass)) {
            return new FlatStringSerializer(beanClass, beanClass.getRecordComponents()[0]);
        }
        return serializer;
    }

    /**
     * Delegates to {@link EvoTypes#isSingleStringEvoRecord(Class)} — the shared
     * predicate in {@code evo-core} used by both Jackson and JSON-B modules.
     */
    static boolean isSingleStringEvoRecord(Class<?> type) {
        return EvoTypes.isSingleStringEvoRecord(type);
    }

    @SuppressWarnings("rawtypes")
    private static class FlatStringSerializer extends StdSerializer<Record> {

        private final RecordComponent component;

        FlatStringSerializer(Class<?> type, RecordComponent component) {
            super(type, false);
            this.component = component;
        }

        @Override
        public void serialize(Record record, JsonGenerator gen, SerializationContext ctxt)
                throws JacksonException {
            try {
                var value = (String) component.getAccessor().invoke(record);
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value);
                }
            } catch (ReflectiveOperationException e) {
                ctxt.reportBadDefinition(handledType(),
                        "Failed to access record component '" + component.getName() + "': " + e.getMessage());
            }
        }
    }
}
