package dev.cominotti.java.evo;

import java.lang.reflect.RecordComponent;

/**
 * Utility methods for introspecting EVO types at runtime.
 *
 * <p>Shared across serialization modules ({@code evo-jackson}, {@code evo-jsonb})
 * to avoid duplicating the detection predicate.</p>
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
}
