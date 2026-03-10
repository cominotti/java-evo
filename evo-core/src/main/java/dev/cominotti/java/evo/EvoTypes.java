// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

/**
 * Utility methods for introspecting and instantiating EVO types at runtime.
 *
 * <p>Shared across serialization modules ({@code evo-jackson}, {@code evo-rest})
 * and used by {@code evo-jsonb} indirectly via the per-type adapter pattern.</p>
 */
public final class EvoTypes {

    private EvoTypes() {}

    /**
     * Returns {@code true} if the type is a Java record annotated with
     * {@link EvoType} with exactly one {@code String} component.
     *
     * <p>Three guards in order:</p>
     * <ol>
     *   <li>Must be a record — eliminates all non-record types</li>
     *   <li>Must carry {@code @EvoType} at runtime — eliminates generic records</li>
     *   <li>Exactly one component of type {@code String} — the EVO value</li>
     * </ol>
     */
    public static boolean isSingleStringEvoRecord(Class<?> type) {
        if (!type.isRecord()) return false;
        if (!type.isAnnotationPresent(EvoType.class)) return false;
        RecordComponent[] components = type.getRecordComponents();
        return components.length == 1 && components[0].getType() == String.class;
    }

    /**
     * Returns the canonical {@code (String)} constructor for an EVO record type.
     *
     * <p>Fails fast with {@link IllegalStateException} if the constructor doesn't exist,
     * which should only happen if the type is not a valid single-String EVO record.</p>
     */
    public static <T> Constructor<T> canonicalStringConstructor(Class<T> type) {
        try {
            return type.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Record " + type.getName() + " has no canonical constructor(String)", e);
        }
    }

    /**
     * Instantiates an EVO record via its canonical {@code (String)} constructor,
     * unwrapping {@link InvocationTargetException} to surface the original
     * {@link IllegalArgumentException} from {@code EvoValidation.validate()}.
     *
     * <p>Callers catch the thrown {@code IllegalArgumentException} and wrap it
     * in their framework-specific exception type (e.g., {@code JsonbException},
     * {@code MismatchedInputException}).</p>
     *
     * @throws IllegalArgumentException if the EVO constructor rejects the value
     * @throws RuntimeException for unexpected reflective errors
     */
    public static <T> T newInstance(Constructor<T> constructor, String value) {
        try {
            return constructor.newInstance(value);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException iae) {
                throw iae;
            }
            throw new IllegalStateException(
                    "Failed to construct " + constructor.getDeclaringClass().getSimpleName()
                            + ": " + e.getCause().getMessage(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to construct " + constructor.getDeclaringClass().getSimpleName()
                            + ": " + e.getMessage(), e);
        }
    }
}
