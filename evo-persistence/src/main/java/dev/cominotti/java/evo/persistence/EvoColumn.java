package dev.cominotti.java.evo.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.AttributeBinderType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Unified column mapping annotation for EVO entity fields.
 *
 * <p>Replaces {@code @Column} for all EVO attributes. Column length is derived automatically
 * from the EVO type's {@code @Size(max = ...)} annotation on its {@code value} component.
 * If no {@code @Size} is present (e.g., types using {@code @Pattern} for length enforcement),
 * the {@link #length()} attribute must be set explicitly.</p>
 *
 * <p>Requires an {@code autoApply} converter for the EVO type to be on the classpath
 * (e.g., {@link EmailConverter}, {@link CpfConverter}). For sealed interface types like
 * {@code CpfOrCnpj}, an explicit {@code @Convert} is also required.</p>
 *
 * <h3>Usage examples</h3>
 *
 * <pre>{@code
 * // Length derived from @Size(max=320) on Email.value
 * @EvoColumn(name = "email")
 * private Email email;
 *
 * // Explicit length — Cpf has no @Size, uses @Pattern instead
 * @EvoColumn(name = "author_cpf", length = CpfRules.DIGIT_COUNT)
 * private Cpf authorCpf;
 *
 * // Override default length for a specific use case
 * @EvoColumn(name = "short_email", length = 100)
 * private Email shortEmail;
 * }</pre>
 *
 * @see EvoColumnBinder
 */
@Documented
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AttributeBinderType(binder = EvoColumnBinder.class)
public @interface EvoColumn {

    /** Column name. Required — no default (entity fields must name their columns). */
    String name();

    /**
     * Column length. Default: {@code -1} (derive from the EVO type's
     * {@code @Size(max = ...)} annotation). Set explicitly for EVO types that
     * use {@code @Pattern} instead of {@code @Size} for length enforcement.
     */
    int length() default -1;

    /**
     * Whether the column allows NULL. Default: {@code false} (NOT NULL).
     *
     * <p>Most DDD value objects are required parts of an entity's invariants,
     * so NOT NULL is the safe default. Set {@code nullable = true} explicitly
     * for optional EVO fields.</p>
     */
    boolean nullable() default false;
}
