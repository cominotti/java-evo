// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.greeting.Greeting;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EvoControllerIntegrationTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void evoModuleIsRegisteredInSpringContext() {
        var email = new Email("test@example.com");
        var json = objectMapper.writeValueAsString(email);
        assertThat(json).isEqualTo("\"test@example.com\"");
    }

    @Test
    void greetingWithEvosSerializesWithFlatStrings() {
        var greeting = new Greeting("Alice", "Hello!");
        greeting.setEmail(new Email("alice@example.com"));
        greeting.setAuthorCpf(new Cpf(VALID_CPF));
        greeting.setCompanyCnpj(new Cnpj(VALID_CNPJ));
        greeting.setTaxId(new Cpf(VALID_CPF));

        var json = objectMapper.writeValueAsString(greeting);

        assertThat(json).contains("\"email\":\"alice@example.com\"");
        assertThat(json).contains("\"authorCpf\":\"" + VALID_CPF + "\"");
        assertThat(json).contains("\"companyCnpj\":\"" + VALID_CNPJ + "\"");
        assertThat(json).contains("\"taxId\":\"" + VALID_CPF + "\"");
        assertThat(json).doesNotContain("{\"value\"");
    }

    @Test
    void greetingWithNullEvosSerializesWithNulls() {
        var greeting = new Greeting("Bob", "Hi!");

        var json = objectMapper.writeValueAsString(greeting);

        assertThat(json).contains("\"email\":null");
        assertThat(json).contains("\"taxId\":null");
    }

    @Test
    void emailDeserializesFromFlatStringInSpringContext() {
        var email = objectMapper.readValue("\"user@example.com\"", Email.class);
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void cpfOrCnpjDeserializesCorrectlyInSpringContext() {
        CpfOrCnpj cpf = objectMapper.readValue("\"" + VALID_CPF + "\"", CpfOrCnpj.class);
        assertThat(cpf).isInstanceOf(Cpf.class);

        CpfOrCnpj cnpj = objectMapper.readValue("\"" + VALID_CNPJ + "\"", CpfOrCnpj.class);
        assertThat(cnpj).isInstanceOf(Cnpj.class);
    }

    // --- @JsonProperty on EVO fields in view objects ---

    /**
     * Test-local record that uses {@code @JsonProperty} to customize the JSON name
     * of an EVO field. Verifies that Jackson annotations compose correctly with
     * {@code SingleValueEvoDeserializer} flat-string handling.
     */
    record CustomNameRequest(
            String name,
            @JsonProperty("contact_email") Email email,
            @JsonProperty("tax_id") CpfOrCnpj taxId
    ) {}

    @Test
    void evoFieldWithJsonPropertyDeserializesFromCustomName() {
        var json = """
                {"name": "Alice", "contact_email": "alice@example.com"}
                """;
        var result = objectMapper.readValue(json, CustomNameRequest.class);
        assertThat(result.email().value()).isEqualTo("alice@example.com");
    }

    @Test
    void evoFieldWithJsonPropertySerializesWithCustomName() {
        var request = new CustomNameRequest("Alice", new Email("alice@example.com"), null);
        var json = objectMapper.writeValueAsString(request);
        assertThat(json).contains("\"contact_email\":\"alice@example.com\"");
        assertThat(json).doesNotContain("\"email\":");
    }

    @Test
    void evoFieldWithJsonPropertyIgnoresOriginalName() {
        var json = """
                {"name": "Alice", "email": "alice@example.com"}
                """;
        var result = objectMapper.readValue(json, CustomNameRequest.class);
        assertThat(result.email()).isNull();
    }

    @Test
    void cpfOrCnpjFieldWithJsonPropertyDeserializesFromCustomName() {
        var json = """
                {"name": "Bob", "tax_id": "%s"}
                """.formatted(VALID_CPF);
        var result = objectMapper.readValue(json, CustomNameRequest.class);
        assertThat(result.taxId()).isInstanceOf(Cpf.class);
        assertThat(result.taxId().value()).isEqualTo(VALID_CPF);
    }

    @Test
    void nullEvoFieldWithJsonPropertySerializesAsNull() {
        var request = new CustomNameRequest("Alice", null, null);
        var json = objectMapper.writeValueAsString(request);
        assertThat(json).contains("\"contact_email\":null");
        assertThat(json).contains("\"tax_id\":null");
    }
}
