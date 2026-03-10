package dev.cominotti.java.evo.greeting;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Unified validation error handler that produces consistent {@link ProblemDetail}
 * responses (RFC 9457) for both EVO deserialization errors and Jakarta Validation errors.
 *
 * <h3>Why unification matters</h3>
 *
 * <p>Without this handler, two different error formats are returned for what API consumers
 * perceive as the same kind of problem ("invalid field"):</p>
 *
 * <ul>
 *   <li><b>EVO error</b> (invalid value during Jackson deserialization): the record's compact
 *       constructor throws {@link IllegalArgumentException}, which Jackson wraps as a
 *       {@link MismatchedInputException}. Spring produces an {@link HttpMessageNotReadableException}
 *       with a generic message.</li>
 *   <li><b>Validation error</b> (constraint violation via {@code @Valid @RequestBody}): Spring
 *       throws {@link MethodArgumentNotValidException} with field-level details, but the default
 *       response uses the legacy error format — not {@link ProblemDetail}.</li>
 * </ul>
 *
 * <p>This handler normalizes both into a single {@link ProblemDetail} structure with a
 * consistent {@code "errors"} array containing {@code field} and {@code message} entries.</p>
 *
 * <h3>EVO cause chain</h3>
 *
 * <p>{@code HttpMessageNotReadableException} → {@code MismatchedInputException} →
 * {@code IllegalArgumentException}. The field name is extracted from the
 * {@code MismatchedInputException}'s JSON path, and the validation message from the
 * chained {@code IllegalArgumentException}.</p>
 */
@RestControllerAdvice
class EvoExceptionHandler {

    private static final String TITLE = "Validation failed";

    /**
     * Handles EVO constructor validation failures during Jackson deserialization.
     *
     * <p>Extracts the field name from {@link MismatchedInputException#getPath()} and
     * the i18n error message from the chained {@link IllegalArgumentException}.</p>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof MismatchedInputException mie
                && mie.getCause() instanceof IllegalArgumentException iae) {
            // Build the full dotted path (e.g., "user.contact.email") so nested
            // EVO fields report the same path format as Jakarta Validation.
            var fieldName = mie.getPath().isEmpty()
                    ? "unknown"
                    : mie.getPath().stream()
                            .map(ref -> ref.getPropertyName())
                            .collect(Collectors.joining("."));
            return validationProblem(List.of(fieldError(fieldName, iae.getMessage())));
        }
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Failed to read request");
    }

    /**
     * Handles Jakarta Validation constraint violations from {@code @Valid @RequestBody}.
     *
     * <p>Converts Spring's {@link MethodArgumentNotValidException} field errors into the
     * same {@code "errors"} array format used for EVO deserialization errors.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return validationProblem(errors);
    }

    private static ProblemDetail validationProblem(List<Map<String, String>> errors) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed with " + errors.size() + " error(s).");
        problem.setTitle(TITLE);
        problem.setProperty("errors", errors);
        return problem;
    }

    private static Map<String, String> fieldError(String field, String message) {
        return Map.of("field", field, "message", message);
    }
}
