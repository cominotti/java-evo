package dev.cominotti.java.evo.rest;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Jakarta REST {@link ExceptionMapper} for Jakarta Validation constraint violations.
 *
 * <p>Maps {@link ConstraintViolationException} (thrown when {@code @Valid} on a
 * resource method parameter detects constraint violations) into the same
 * {@link ValidationProblem} format used by {@link EvoJsonbExceptionMapper}.</p>
 *
 * <h3>Property path stripping</h3>
 *
 * <p>In Jakarta REST, {@code ConstraintViolation.getPropertyPath()} includes the
 * resource method name and parameter index (e.g., {@code createGreeting.arg0.name}).
 * This mapper strips {@code METHOD} and {@code PARAMETER} nodes, keeping only
 * {@code PROPERTY} nodes to produce clean field names like {@code "name"} or
 * {@code "contact.workEmail"}.</p>
 */
@Provider
public class EvoConstraintViolationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        var errors = exception.getConstraintViolations().stream()
                .map(cv -> new ValidationProblem.FieldError(
                        extractFieldPath(cv.getPropertyPath()),
                        cv.getMessage()))
                .toList();
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ValidationProblem.of(errors))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Extracts the field path from a Jakarta Validation {@link Path}, stripping
     * method and parameter nodes.
     *
     * <p>Path nodes for a resource method parameter look like:
     * {@code [METHOD:createGreeting, PARAMETER:arg0, PROPERTY:name]}. We keep only
     * {@code PROPERTY} nodes, joined with dots.</p>
     */
    static String extractFieldPath(Path path) {
        var fieldPath = StreamSupport.stream(path.spliterator(), false)
                .filter(node -> node.getKind() == ElementKind.PROPERTY)
                .map(Path.Node::getName)
                .collect(Collectors.joining("."));
        return fieldPath.isEmpty() ? ValidationProblem.UNKNOWN_FIELD : fieldPath;
    }
}
