package dev.cominotti.java.evo.rest;

import java.util.List;

/**
 * RFC 9457-like response body for validation errors in Jakarta REST.
 *
 * <p>Matches the {@code ProblemDetail} format produced by {@code EvoExceptionHandler}
 * in the Spring MVC example ({@code evo-example}), ensuring consistent error responses
 * across both Spring MVC and Jakarta REST stacks:</p>
 *
 * <pre>{@code
 * {
 *   "title": "Validation failed",
 *   "status": 400,
 *   "detail": "Request validation failed with 1 error(s).",
 *   "errors": [
 *     {"field": "email", "message": "Email must be a valid email address"}
 *   ]
 * }
 * }</pre>
 */
public record ValidationProblem(
        String title,
        int status,
        String detail,
        List<FieldError> errors
) {

    /** Fallback field name when the actual field cannot be determined. */
    public static final String UNKNOWN_FIELD = "unknown";

    public record FieldError(String field, String message) {}

    /**
     * Creates a {@code ValidationProblem} with the standard title and status.
     */
    public static ValidationProblem of(List<FieldError> errors) {
        return new ValidationProblem(
                "Validation failed",
                400,
                "Request validation failed with " + errors.size() + " error(s).",
                errors);
    }
}
