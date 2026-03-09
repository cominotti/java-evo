package dev.cominotti.java.evo;

import dev.cominotti.java.evo.greeting.Greeting;
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
}
