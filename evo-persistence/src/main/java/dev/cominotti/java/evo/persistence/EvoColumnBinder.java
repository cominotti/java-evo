package dev.cominotti.java.evo.persistence;

import jakarta.validation.constraints.Size;
import org.hibernate.binder.AttributeBinder;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;

/**
 * Hibernate {@link AttributeBinder} for {@link EvoColumn}.
 *
 * <p>Sets column name and nullable from the annotation attributes. Column length is resolved
 * in two ways:</p>
 *
 * <ol>
 *   <li>If {@link EvoColumn#length()} is explicitly set ({@code > 0}), that value is used.</li>
 *   <li>Otherwise, the length is derived from the EVO type's {@code @Size(max = ...)}
 *       annotation on its {@code value} field. This leverages Jakarta Validation's
 *       {@code @Size} as the single source of truth for both validation and column DDL.</li>
 * </ol>
 *
 * <p>If neither an explicit length nor a {@code @Size} annotation is available, the column
 * length defaults to 255 (the JPA {@code @Column} default). This should only happen for
 * EVO types that use {@code @Pattern} instead of {@code @Size} — in which case the developer
 * should always provide an explicit {@code length} in the {@code @EvoColumn} annotation.</p>
 *
 * <p>The EVO type is resolved via reflection from the entity class and the property name.
 * For record EVOs, the {@code @Size} annotation is read from the declared field (not the
 * record component), because Jakarta Validation annotations propagate to fields, not to
 * record components.</p>
 */
public class EvoColumnBinder implements AttributeBinder<EvoColumn> {

    private static final int DERIVE_FROM_SIZE = -1;
    private static final int JPA_COLUMN_DEFAULT_LENGTH = 255;

    @Override
    public void bind(EvoColumn annotation,
                     MetadataBuildingContext buildingContext,
                     PersistentClass persistentClass,
                     Property property) {

        var column = (Column) property.getColumns().get(0);
        column.setName(annotation.name());
        column.setNullable(annotation.nullable());

        if (annotation.length() != DERIVE_FROM_SIZE) {
            column.setLength(annotation.length());
        } else {
            column.setLength(deriveLengthFromEvoType(persistentClass, property));
        }
    }

    /**
     * Derives the column length from the EVO type's {@code @Size(max = ...)} annotation.
     *
     * <p>Resolution path: entity class → declared field → field type (EVO class) →
     * declared {@code "value"} field → {@code @Size} annotation → {@code max()} value.</p>
     *
     * @return the derived length, or {@value JPA_COLUMN_DEFAULT_LENGTH} if no {@code @Size}
     *         annotation is found
     */
    private static int deriveLengthFromEvoType(PersistentClass persistentClass,
                                               Property property) {
        try {
            var entityClass = persistentClass.getMappedClass();
            var entityField = entityClass.getDeclaredField(property.getName());
            var evoType = entityField.getType();

            // EVO records have a single "value" field — @Size propagates to the field,
            // not the record component (see AGENTS.md: "Jakarta Validation annotations
            // don't target RECORD_COMPONENT").
            var valueField = evoType.getDeclaredField("value");
            var sizeAnnotation = valueField.getAnnotation(Size.class);

            if (sizeAnnotation != null) {
                return sizeAnnotation.max();
            }
        } catch (NoSuchFieldException ignored) {
            // EVO type has no "value" field (e.g., sealed interface like CpfOrCnpj),
            // or entity field name doesn't match — fall through to default.
        }

        return JPA_COLUMN_DEFAULT_LENGTH;
    }
}
