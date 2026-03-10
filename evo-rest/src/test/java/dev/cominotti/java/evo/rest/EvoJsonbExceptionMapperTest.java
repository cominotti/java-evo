package dev.cominotti.java.evo.rest;

import jakarta.json.bind.JsonbException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvoJsonbExceptionMapperTest {

    private final EvoJsonbExceptionMapper mapper = new EvoJsonbExceptionMapper();

    @Test
    void evoValidationFailureReturnsBadRequestWithFieldError() {
        var iae = new IllegalArgumentException("Email must be a valid email address");
        var jsonbEx = new JsonbException("Error deserializing object at property 'email'", iae);

        var response = mapper.toResponse(jsonbEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(body.title()).isEqualTo("Validation failed");
        assertThat(body.errors()).hasSize(1);
        assertThat(body.errors().getFirst().field()).isEqualTo("email");
        assertThat(body.errors().getFirst().message()).isEqualTo("Email must be a valid email address");
    }

    @Test
    void extractsFieldNameFromYassonWrappedCauseChain() {
        // Yasson wraps: JsonbException → InvocationTargetException → IllegalArgumentException
        var iae = new IllegalArgumentException("must not be blank");
        var ite = new java.lang.reflect.InvocationTargetException(iae);
        var jsonbEx = new JsonbException("Error deserializing object at property 'name'", ite);

        var response = mapper.toResponse(jsonbEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(body.errors().getFirst().field()).isEqualTo("name");
        assertThat(body.errors().getFirst().message()).isEqualTo("must not be blank");
    }

    @Test
    void unknownFieldNameWhenMessageLacksPropertyInfo() {
        var iae = new IllegalArgumentException("invalid");
        var jsonbEx = new JsonbException("Some other error", iae);

        var response = mapper.toResponse(jsonbEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(body.errors().getFirst().field()).isEqualTo("unknown");
        assertThat(body.errors().getFirst().message()).isEqualTo("invalid");
    }

    @Test
    void nonEvoJsonbExceptionReturnsGenericBadRequest() {
        var jsonbEx = new JsonbException("Unexpected end of input");

        var response = mapper.toResponse(jsonbEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(body.title()).isEqualTo("Bad Request");
        assertThat(body.detail()).isEqualTo("Failed to read request");
        assertThat(body.errors()).isEmpty();
    }

    @Test
    void extractFieldNameParsesYassonFormat() {
        assertThat(EvoJsonbExceptionMapper.extractFieldName(
                "Error deserializing object at property 'email'")).isEqualTo("email");
        assertThat(EvoJsonbExceptionMapper.extractFieldName(
                "Error at property 'contact'")).isEqualTo("contact");
        assertThat(EvoJsonbExceptionMapper.extractFieldName(
                "No property info")).isEqualTo("unknown");
        assertThat(EvoJsonbExceptionMapper.extractFieldName(null)).isEqualTo("unknown");
    }
}
