// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.rest;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvoJsonbExceptionMapperTest {

    private final EvoJsonbExceptionMapper mapper = new EvoJsonbExceptionMapper();

    @Test
    void evoValidationFailureReturnsBadRequestWithFieldError() {
        var iae = new IllegalArgumentException("Email must be a valid email address");
        var jsonbEx = new JsonbException("Unable to deserialize property 'email' because of: ...", iae);
        var processingEx = new ProcessingException(jsonbEx);

        var response = mapper.toResponse(processingEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(body.title()).isEqualTo("Validation failed");
        assertThat(body.errors()).hasSize(1);
        assertThat(body.errors().getFirst().field()).isEqualTo("email");
        assertThat(body.errors().getFirst().message()).isEqualTo("Email must be a valid email address");
    }

    @Test
    void extractsFieldNameFromYassonWrappedCauseChain() {
        // Real Yasson chain: ProcessingException → JsonbException → JsonbException → IAE
        var iae = new IllegalArgumentException("must not be blank");
        var innerJsonb = new JsonbException("adapter error", iae);
        var outerJsonb = new JsonbException(
                "Unable to deserialize property 'name' because of: adapter error", innerJsonb);
        var processingEx = new ProcessingException(outerJsonb);

        var response = mapper.toResponse(processingEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(body.errors().getFirst().field()).isEqualTo("name");
        assertThat(body.errors().getFirst().message()).isEqualTo("must not be blank");
    }

    @Test
    void unknownFieldNameWhenMessageLacksPropertyInfo() {
        var iae = new IllegalArgumentException("invalid");
        var jsonbEx = new JsonbException("Some other error", iae);
        var processingEx = new ProcessingException(jsonbEx);

        var response = mapper.toResponse(processingEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(body.errors().getFirst().field()).isEqualTo("unknown");
        assertThat(body.errors().getFirst().message()).isEqualTo("invalid");
    }

    @Test
    void nonEvoProcessingExceptionReturnsGenericBadRequest() {
        var processingEx = new ProcessingException("Unexpected end of input");

        var response = mapper.toResponse(processingEx);
        var body = (ValidationProblem) response.getEntity();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(body.title()).isEqualTo("Bad Request");
        assertThat(body.detail()).isEqualTo("Failed to read request");
        assertThat(body.errors()).isEmpty();
    }

    @Test
    void extractFieldNameParsesYassonFormat() {
        assertThat(EvoJsonbExceptionMapper.extractFieldName(
                "Unable to deserialize property 'email' because of: ...")).isEqualTo("email");
        assertThat(EvoJsonbExceptionMapper.extractFieldName(
                "Error at property 'contact'")).isEqualTo("contact");
        assertThat(EvoJsonbExceptionMapper.extractFieldName(
                "No property info")).isEqualTo("unknown");
        assertThat(EvoJsonbExceptionMapper.extractFieldName(null)).isEqualTo("unknown");
    }
}
