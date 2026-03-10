package dev.cominotti.java.evo.jsonb;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * Abstract base for JSON-B adapters that serialize {@code @EvoType} records as
 * flat JSON strings.
 *
 * <p>Concrete subclasses are one-liners that supply the constructor and accessor
 * method references — the same pattern used by {@code StringEvoConverter<T>} in
 * {@code evo-persistence}:</p>
 *
 * <pre>{@code
 * public class EmailJsonbAdapter extends StringEvoJsonbAdapter<Email> {
 *     public EmailJsonbAdapter() { super(Email::value, Email::new); }
 * }
 * }</pre>
 *
 * <h3>Why concrete subclasses are required</h3>
 *
 * <p>JSON-B matches adapters by resolving the generic type parameter {@code T} from
 * the adapter class's metadata. A generic {@code StringEvoJsonbAdapter<T>} erases
 * {@code T} to {@code Record}, so JSON-B cannot determine which EVO type the adapter
 * handles. Concrete subclasses like {@code EmailJsonbAdapter extends
 * StringEvoJsonbAdapter<Email>} retain {@code Email} in the class metadata, enabling
 * correct type matching.</p>
 *
 * <h3>Error handling</h3>
 *
 * <p>When the EVO constructor throws {@link IllegalArgumentException} (from
 * {@code EvoValidation.validate()}), the adapter wraps it in a {@link JsonbException}
 * with the original IAE as the cause. This mirrors the {@code initCause()} pattern
 * in {@code SingleValueEvoDeserializer} (Jackson), enabling downstream
 * {@code ExceptionMapper}s to extract the i18n validation message.</p>
 *
 * @param <T> the concrete EVO record type
 */
public abstract class StringEvoJsonbAdapter<T> implements JsonbAdapter<T, String> {

    private final EvoAccessor<T> accessor;
    private final Constructor<T> constructor;

    /**
     * @param accessor function that extracts the String value from an EVO instance
     *                 (typically {@code Email::value})
     * @param type     the EVO record class (needed to look up the canonical constructor)
     */
    protected StringEvoJsonbAdapter(EvoAccessor<T> accessor, Class<T> type) {
        this.accessor = accessor;
        try {
            this.constructor = type.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Record " + type.getName() + " has no canonical constructor(String)", e);
        }
    }

    @Override
    public String adaptToJson(T obj) {
        return obj == null ? null : accessor.value(obj);
    }

    @Override
    public T adaptFromJson(String value) {
        if (value == null) return null;
        try {
            return constructor.newInstance(value);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException iae) {
                throw new JsonbException(iae.getMessage(), iae);
            }
            throw new JsonbException(
                    "Failed to construct " + constructor.getDeclaringClass().getSimpleName()
                            + ": " + e.getCause().getMessage(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new JsonbException(
                    "Failed to construct " + constructor.getDeclaringClass().getSimpleName()
                            + ": " + e.getMessage(), e);
        }
    }

    /**
     * Functional interface for extracting the String value from an EVO instance.
     * Using a dedicated interface instead of {@code Function<T, String>} to keep
     * the constructor signature unambiguous.
     */
    @FunctionalInterface
    public interface EvoAccessor<T> {
        String value(T evo);
    }
}
