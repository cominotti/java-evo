package dev.cominotti.java.evo.rest;

import java.util.List;
import java.util.regex.Pattern;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Jakarta REST {@link ExceptionMapper} for EVO deserialization errors.
 *
 * <p>When an invalid JSON string is deserialized into an EVO, the record's compact
 * constructor throws {@link IllegalArgumentException}. JSON-B (Yasson) wraps this as
 * a {@link JsonbException}. This mapper extracts the original validation message from
 * the cause chain and returns a {@link ValidationProblem} response.</p>
 *
 * <h3>Field name extraction</h3>
 *
 * <p>Unlike Jackson's {@code MismatchedInputException} which provides a structured
 * {@code getPath()} API, {@code JsonbException} carries only a text message. The
 * field name is extracted via regex from Yasson's message format
 * ({@code "...property 'fieldName'"}). If the regex doesn't match (different JSON-B
 * implementation or unexpected format), the field name falls back to
 * {@code "unknown"}.</p>
 *
 * <h3>Known limitations</h3>
 *
 * <ul>
 *   <li>Nested field paths: Yasson reports only the immediate property name, not
 *       the full dotted path (e.g., {@code "email"} instead of
 *       {@code "contact.address.email"})</li>
 *   <li>Field name extraction is Yasson-specific; other JSON-B implementations
 *       may degrade to {@code "unknown"}</li>
 * </ul>
 */
@Provider
public class EvoJsonbExceptionMapper implements ExceptionMapper<JsonbException> {

    /** Matches Yasson's message format: "...property 'fieldName'" */
    private static final Pattern YASSON_PROPERTY_PATTERN =
            Pattern.compile("property '([^']+)'");

    @Override
    public Response toResponse(JsonbException exception) {
        var iae = findIllegalArgumentException(exception);
        if (iae != null) {
            var fieldName = extractFieldName(exception.getMessage());
            var error = new ValidationProblem.FieldError(fieldName, iae.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ValidationProblem.of(List.of(error)))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Not an EVO validation error — generic JSON parse failure.
        var problem = new ValidationProblem("Bad Request", 400,
                "Failed to read request", List.of());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(problem)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Walks the full cause chain looking for an {@link IllegalArgumentException}.
     * Yasson may wrap with {@link java.lang.reflect.InvocationTargetException} between
     * the {@code JsonbException} and the IAE, so we check beyond just {@code getCause()}.
     */
    private static IllegalArgumentException findIllegalArgumentException(Throwable t) {
        var current = t.getCause();
        while (current != null) {
            if (current instanceof IllegalArgumentException iae) {
                return iae;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * Extracts the field name from Yasson's exception message using regex.
     * Returns {@code "unknown"} if the message doesn't match the expected format.
     */
    static String extractFieldName(String message) {
        if (message == null) return "unknown";
        var matcher = YASSON_PROPERTY_PATTERN.matcher(message);
        return matcher.find() ? matcher.group(1) : "unknown";
    }
}
