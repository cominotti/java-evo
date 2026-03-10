// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.EvoTypes;
import jakarta.validation.constraints.Size;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Property;

/**
 * Hibernate {@link Integrator} that auto-derives column lengths for {@code @EvoType} fields.
 *
 * <p>When an entity field holds a single-String EVO record (detected via
 * {@link EvoTypes#isSingleStringEvoRecord(Class)}), this integrator reads the
 * {@code @Size(max = ...)} annotation from the EVO's {@code value} field and applies
 * it as the column length — but only if the column still has the JPA default length (255).
 * If the entity explicitly sets {@code @Column(length = N)} where {@code N != 255},
 * the explicit value is preserved.</p>
 *
 * <p>This eliminates the need for a custom column annotation ({@code @EvoColumn}) —
 * consumers use standard {@code @Column} and get automatic length derivation for free.
 * For EVO types that use {@code @Pattern} instead of {@code @Size} (e.g., {@code Cpf},
 * {@code Cnpj}), the entity must set {@code @Column(length = ...)} explicitly.</p>
 *
 * <p>Auto-discovered via {@code META-INF/services/org.hibernate.integrator.spi.Integrator}
 * when {@code evo-persistence} is on the classpath.</p>
 *
 * @see EvoTypes#isSingleStringEvoRecord(Class)
 */
public class EvoColumnMetadataIntegrator implements Integrator {

    /**
     * JPA default column length — the sentinel value indicating no explicit length was set.
     * When a column has this length and the EVO type has {@code @Size(max)}, the integrator
     * overrides with the derived value.
     */
    private static final long JPA_DEFAULT_LENGTH = 255L;

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                          SessionFactoryImplementor sessionFactory) {
        for (var entityBinding : metadata.getEntityBindings()) {
            // Resolve the mapped class once per entity — it's the same for all properties.
            // Dynamic entities (no mapped class) are skipped entirely.
            Class<?> entityClass;
            try {
                entityClass = entityBinding.getMappedClass();
            } catch (Exception _) {
                continue;
            }

            for (var property : entityBinding.getProperties()) {
                adjustColumnForEvoType(entityClass, property);
            }
        }
    }

    /**
     * If the property holds an EVO type and the column length is still the JPA default (255),
     * overrides the length with the value derived from {@code @Size(max)} on the EVO's
     * {@code value} field.
     *
     * <p>Resolution path: entity class (+ superclasses) → declared field → field type (EVO class) →
     * declared {@code "value"} field → {@code @Size} annotation → {@code max()} value.</p>
     */
    private void adjustColumnForEvoType(Class<?> entityClass, Property property) {
        // Walk the class hierarchy to find the declared field — handles @MappedSuperclass
        // inheritance where the EVO field may be declared on a parent class.
        Class<?> fieldType = findDeclaredFieldType(entityClass, property.getName());
        if (fieldType == null || !EvoTypes.isSingleStringEvoRecord(fieldType)) {
            return;
        }

        var columns = property.getColumns();
        if (columns.isEmpty()) {
            return;
        }

        var column = columns.get(0);

        // Only override if no explicit @Column(length = N) was set. Hibernate leaves
        // Column.getLength() as null when no length is specified, or sets it to 255 (the
        // JPA default from @Column.length()). Both cases mean "no explicit length."
        var length = column.getLength();
        if (length == null || length == JPA_DEFAULT_LENGTH) {
            var derivedLength = deriveLengthFromSizeAnnotation(fieldType);
            if (derivedLength > 0) {
                column.setLength(derivedLength);
            }
        }
    }

    /**
     * Walks the class hierarchy to find a declared field, returning its type.
     * Handles {@code @MappedSuperclass} where EVO fields may be declared on a parent class.
     *
     * @return the field's type, or {@code null} if not found in the hierarchy
     */
    static Class<?> findDeclaredFieldType(Class<?> clazz, String fieldName) {
        for (var current = clazz; current != null && current != Object.class;
             current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName).getType();
            } catch (NoSuchFieldException _) {
                // Field not on this class — try the superclass.
            }
        }
        return null;
    }

    /**
     * Reads {@code @Size(max = ...)} from the EVO type's {@code value} field.
     *
     * <p>Jakarta Validation annotations on records propagate to fields, not to record
     * components, so we read from the declared field rather than the record component.</p>
     *
     * @return the {@code max} value from {@code @Size}, or {@code -1} if not present
     */
    static int deriveLengthFromSizeAnnotation(Class<?> evoType) {
        try {
            var valueField = evoType.getDeclaredField("value");
            var size = valueField.getAnnotation(Size.class);

            // Guard against @Size without an explicit max — the default is Integer.MAX_VALUE,
            // which would produce nonsensical DDL (e.g., VARCHAR(2147483647)).
            if (size != null && size.max() != Integer.MAX_VALUE) {
                return size.max();
            }
        } catch (NoSuchFieldException _) {
            // Sealed interface or non-standard EVO — no "value" field to inspect.
        }
        return -1;
    }
}
