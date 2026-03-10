// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import dev.cominotti.java.evo.EvoTypes;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/**
 * Jakarta REST {@link ParamConverterProvider} for EVO types in
 * {@code @QueryParam}, {@code @PathParam}, {@code @FormParam}, etc.
 *
 * <p>Enables usage like {@code @QueryParam("email") Email email} in Jakarta REST
 * resource methods. The converter calls the EVO's canonical constructor, which
 * triggers validation — invalid values produce {@link IllegalArgumentException},
 * which Jakarta REST maps to 404 (for {@code @PathParam}) or 400 (for
 * {@code @QueryParam}) by default.</p>
 *
 * <h3>Null handling</h3>
 *
 * <p>{@code fromString(null)} returns {@code null} — optional parameters remain
 * null when absent. The EVO constructor is NOT called for null inputs, avoiding
 * the {@code @NotBlank} rejection that would occur if null were passed.</p>
 */
@Provider
public class EvoParamConverterProvider implements ParamConverterProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
                                              Annotation[] annotations) {
        if (rawType == CpfOrCnpj.class) {
            return (ParamConverter<T>) new CpfOrCnpjParamConverter();
        }
        if (EvoTypes.isSingleStringEvoRecord(rawType)) {
            return new EvoRecordParamConverter<>(rawType);
        }
        return null;
    }

    /**
     * Generic {@link ParamConverter} for single-String {@code @EvoType} records.
     * Uses reflection to call the canonical {@code (String)} constructor.
     */
    private static class EvoRecordParamConverter<T> implements ParamConverter<T> {

        private final java.lang.reflect.Constructor<T> constructor;
        private final java.lang.reflect.Method accessor;

        EvoRecordParamConverter(Class<T> type) {
            this.constructor = EvoTypes.canonicalStringConstructor(type);
            this.accessor = type.getRecordComponents()[0].getAccessor();
        }

        @Override
        public T fromString(String value) {
            if (value == null) return null;
            return EvoTypes.newInstance(constructor, value);
        }

        @Override
        public String toString(T value) {
            if (value == null) return null;
            try {
                return (String) accessor.invoke(value);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }

    /**
     * Specialized {@link ParamConverter} for the {@link CpfOrCnpj} sealed interface.
     * Delegates to {@link CpfOrCnpj#of(String)} for length-based dispatch.
     */
    private static class CpfOrCnpjParamConverter implements ParamConverter<CpfOrCnpj> {

        @Override
        public CpfOrCnpj fromString(String value) {
            if (value == null) return null;
            return CpfOrCnpj.of(value);
        }

        @Override
        public String toString(CpfOrCnpj value) {
            return value == null ? null : value.value();
        }
    }
}
