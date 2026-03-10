// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.validation;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Centralized Jakarta Validation entry point for EVO record constructors.
 *
 * <p>EVO compact constructors call {@link #validate(Class, String, Object)} which uses
 * {@link Validator#validateValue} to evaluate all annotation constraints on the record's
 * property — without requiring a fully-constructed bean instance.</p>
 *
 * <h3>Thread safety — double-checked locking (DCL)</h3>
 *
 * <p>Both {@code factory} and {@code validator} are {@code volatile} to ensure safe publication
 * across threads. The {@link #validator()} method uses DCL: a local variable reads the volatile
 * field once (avoiding the double-read penalty), then synchronizes only on first initialization.
 * {@link #setValidatorFactory(ValidatorFactory)} also synchronizes on the same monitor to prevent
 * a race where a concurrent {@code validator()} call could overwrite an externally-set factory
 * with the default one.</p>
 *
 * <h3>ValidatorFactory lifecycle</h3>
 *
 * <p>The {@link ValidatorFactory} returned by {@link Validation#buildDefaultValidatorFactory()}
 * implements {@link java.io.Closeable}. We store the factory reference to avoid a resource leak
 * (Hibernate Validator logs {@code HV000184} when a factory is created but never closed).
 * Since the factory is application-scoped and lives for the JVM lifetime, it is not explicitly
 * closed — this is acceptable for singleton holders, but the reference is retained so it
 * <em>could</em> be closed if a shutdown hook were added in the future.</p>
 */
public final class EvoValidation {

    private static volatile ValidatorFactory factory;
    private static volatile Validator validator;

    private EvoValidation() {}

    /**
     * Replaces the default {@link ValidatorFactory} with a custom one.
     *
     * <p>Synchronized on the class monitor to avoid a race with the lazy initialization
     * in {@link #validator()}. Without this, a concurrent first call to {@code validator()}
     * could enter its {@code synchronized} block, see {@code null}, and overwrite the
     * custom factory with the default one.</p>
     */
    public static void setValidatorFactory(ValidatorFactory customFactory) {
        synchronized (EvoValidation.class) {
            factory = customFactory;
            validator = customFactory.getValidator();
        }
    }

    /**
     * Returns the cached {@link Validator}, lazily initializing from the default factory
     * if needed. Uses double-checked locking with a local variable to minimize volatile reads.
     */
    private static Validator validator() {
        var v = validator;
        if (v != null) {
            return v;
        }
        synchronized (EvoValidation.class) {
            if (validator == null) {
                factory = Validation.buildDefaultValidatorFactory();
                validator = factory.getValidator();
            }
            return validator;
        }
    }

    /**
     * Validates a property value against the constraints declared on a bean type's property.
     *
     * <p>Uses {@link Validator#validateValue} so the bean does not need to be constructed yet —
     * this is essential for record compact constructors where {@code this.value} is not assigned
     * until after the constructor body completes.</p>
     *
     * @throws IllegalArgumentException if any constraint violations are found, with all
     *         violation messages joined by {@code "; "}
     */
    public static <T> void validate(Class<T> type, String property, Object value) {
        var violations = validator().validateValue(type, property, value);
        if (!violations.isEmpty()) {
            var message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}
