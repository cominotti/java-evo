// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.rest;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvoConstraintViolationExceptionMapperTest {

    private final EvoConstraintViolationExceptionMapper mapper =
            new EvoConstraintViolationExceptionMapper();

    /**
     * Test bean for generating real constraint violations via Jakarta Validation.
     * Using actual validation (not mocks) ensures property paths match real behavior.
     */
    record TestRequest(@NotBlank String name, @NotBlank String email) {}

    @Test
    void singleViolationReturnsBadRequestWithOneError() {
        var violations = validate(new TestRequest("", "valid@example.com"));

        var response = mapper.toResponse(new ConstraintViolationException(violations));
        var body = (ValidationProblem) response.getEntity();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(body.title()).isEqualTo("Validation failed");
        assertThat(body.errors()).hasSize(1);
        assertThat(body.errors().getFirst().field()).isEqualTo("name");
    }

    @Test
    void multipleViolationsReturnsBadRequestWithMultipleErrors() {
        var violations = validate(new TestRequest("", ""));

        var response = mapper.toResponse(new ConstraintViolationException(violations));
        var body = (ValidationProblem) response.getEntity();

        assertThat(body.errors()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void fieldPathFromConstraintViolationIsExtracted() {
        var violations = validate(new TestRequest("", "valid@example.com"));
        var violation = violations.iterator().next();

        var fieldPath = EvoConstraintViolationExceptionMapper.extractFieldPath(
                violation.getPropertyPath());

        assertThat(fieldPath).isEqualTo("name");
    }

    @SuppressWarnings("unchecked")
    private static <T> Set<ConstraintViolation<T>> validate(T object) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return (Set<ConstraintViolation<T>>) (Set<?>) factory.getValidator().validate(object);
        }
    }
}
