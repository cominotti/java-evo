// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import java.util.function.Function;

import jakarta.persistence.AttributeConverter;

/**
 * Abstract base for single-component EVO converters.
 *
 * <p>Each concrete converter is a one-liner that passes the EVO's accessor and constructor
 * references. Null-safety is handled once here — both {@code convertToDatabaseColumn} and
 * {@code convertToEntityAttribute} return {@code null} for {@code null} inputs.</p>
 *
 * <p>All concrete subclasses use {@code @Converter(autoApply = true)}, which is safe because
 * EVOs are no longer {@code @Embeddable}. The Hibernate 7 conflict that previously forced
 * {@code autoApply = false} on sealed interface converters (where concrete subtypes were
 * {@code @Embeddable}) no longer applies.</p>
 *
 * @param <T> the EVO type (e.g., {@code Email}, {@code Cpf})
 */
public abstract class StringEvoConverter<T> implements AttributeConverter<T, String> {

    private final Function<T, String> toDb;
    private final Function<String, T> fromDb;

    protected StringEvoConverter(Function<T, String> toDb, Function<String, T> fromDb) {
        this.toDb = toDb;
        this.fromDb = fromDb;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return attribute == null ? null : toDb.apply(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        return dbData == null ? null : fromDb.apply(dbData);
    }
}
