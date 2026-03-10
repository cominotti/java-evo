package dev.cominotti.java.evo.jackson;

import java.lang.reflect.Constructor;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson {@link ValueDeserializerModifier} that deserializes flat JSON strings
 * into single-component EVO records.
 *
 * <p>When a JSON string is encountered for a type matching
 * {@link SingleValueEvoSerializer#isSingleStringEvoRecord(Class)}, the string is passed
 * to the record's canonical constructor via reflection. This triggers the compact
 * constructor, which validates the value through {@code EvoValidation.validate()} —
 * so invalid JSON strings are rejected with meaningful error messages.</p>
 */
class SingleValueEvoDeserializer extends ValueDeserializerModifier {

    @Override
    public ValueDeserializer<?> modifyDeserializer(
            DeserializationConfig config,
            BeanDescription.Supplier beanDesc,
            ValueDeserializer<?> deserializer) {

        Class<?> beanClass = beanDesc.getBeanClass();
        if (SingleValueEvoSerializer.isSingleStringEvoRecord(beanClass)) {
            return new FlatStringDeserializer(beanClass);
        }
        return deserializer;
    }

    @SuppressWarnings("rawtypes")
    private static class FlatStringDeserializer extends StdDeserializer<Record> {

        private final Constructor<?> canonicalConstructor;

        FlatStringDeserializer(Class<?> type) {
            super(type);
            try {
                this.canonicalConstructor = type.getDeclaredConstructor(String.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "Record " + type.getName() + " has no canonical constructor(String)", e);
            }
        }

        @Override
        public Record deserialize(JsonParser p, DeserializationContext ctxt)
                throws JacksonException {
            String value = p.getValueAsString();
            try {
                return (Record) canonicalConstructor.newInstance(value);
            } catch (ReflectiveOperationException e) {
                if (e.getCause() instanceof IllegalArgumentException iae) {
                    // Chain the original IAE as the cause so that downstream error
                    // handlers (e.g., a @ControllerAdvice) can extract the EVO
                    // validation message without parsing the MismatchedInputException text.
                    var ex = ctxt.weirdStringException(value, handledType(), iae.getMessage());
                    ex.initCause(iae);
                    throw ex;
                }
                throw ctxt.weirdStringException(value, handledType(),
                        "Failed to construct " + handledType().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
