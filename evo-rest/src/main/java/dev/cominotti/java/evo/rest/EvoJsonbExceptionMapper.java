// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.rest;

import java.util.List;
import java.util.regex.Pattern;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Jakarta REST {@link ExceptionMapper} for EVO deserialization errors.
 *
 * <p>When an invalid JSON string is deserialized into an EVO, the record's compact
 * constructor throws {@link IllegalArgumentException}. JSON-B (Yasson) wraps this as
 * a {@link JsonbException}, and Jersey further wraps it as a {@link ProcessingException}
 * (because the exception originates in a {@code MessageBodyReader}). This mapper handles
 * the outer {@code ProcessingException} and walks the cause chain to find the EVO
 * validation message.</p>
 *
 * <h3>Exception chain</h3>
 *
 * <p>{@code ProcessingException} → {@code JsonbException} (Yasson context) →
 * {@code JsonbException} (adapter wrapping) → {@code IllegalArgumentException}
 * (the EVO validation error from {@code EvoValidation.validate()}).</p>
 *
 * <h3>Field name extraction</h3>
 *
 * <p>Unlike Jackson's {@code MismatchedInputException} which provides a structured
 * {@code getPath()} API, {@code JsonbException} carries only a text message. The
 * field name is extracted via regex from Yasson's message format
 * ({@code "...property 'fieldName'"}). If the regex doesn't match (different JSON-B
 * implementation or unexpected format), the field name falls back to
 * {@code ValidationProblem.UNKNOWN_FIELD}.</p>
 *
 * <h3>Known limitations</h3>
 *
 * <ul>
 *   <li>Nested field paths: Yasson reports only the immediate property name, not
 *       the full dotted path (e.g., {@code "email"} instead of
 *       {@code "contact.address.email"})</li>
 *   <li>Field name extraction is Yasson-specific; other JSON-B implementations
 *       may degrade to {@code ValidationProblem.UNKNOWN_FIELD}</li>
 * </ul>
 */
@Provider
public class EvoJsonbExceptionMapper implements ExceptionMapper<ProcessingException> {

    /** Matches Yasson's message format: "...property 'fieldName'" */
    private static final Pattern YASSON_PROPERTY_PATTERN =
            Pattern.compile("property '([^']+)'");

    @Override
    public Response toResponse(ProcessingException exception) {
        var iae = findInCauseChain(exception, IllegalArgumentException.class);
        if (iae != null) {
            var jsonbEx = findInCauseChain(exception, JsonbException.class);
            var fieldName = jsonbEx != null
                    ? extractFieldName(jsonbEx.getMessage())
                    : ValidationProblem.UNKNOWN_FIELD;
            var error = new ValidationProblem.FieldError(fieldName, iae.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ValidationProblem.of(List.of(error)))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Not an EVO validation error — generic processing failure.
        var problem = new ValidationProblem("Bad Request", 400,
                "Failed to read request", List.of());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(problem)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Walks the full cause chain looking for an exception of the given type.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T findInCauseChain(Throwable t, Class<T> type) {
        var current = t;
        while (current != null) {
            if (type.isInstance(current)) {
                return (T) current;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * Extracts the field name from Yasson's exception message using regex.
     * Returns {@code ValidationProblem.UNKNOWN_FIELD} if the message doesn't match the expected format.
     */
    static String extractFieldName(String message) {
        if (message == null) return ValidationProblem.UNKNOWN_FIELD;
        var matcher = YASSON_PROPERTY_PATTERN.matcher(message);
        return matcher.find() ? matcher.group(1) : ValidationProblem.UNKNOWN_FIELD;
    }
}
