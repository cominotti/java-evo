// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvoJsonbTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    private Jsonb jsonb;

    @BeforeEach
    void setUp() {
        jsonb = JsonbBuilder.create(EvoJsonbConfig.withDefaults(new JsonbConfig()));
    }

    @AfterEach
    void tearDown() throws Exception {
        jsonb.close();
    }

    // --- Email serialization ---

    @Test
    void emailSerializesToFlatString() {
        var email = new Email("user@example.com");
        assertThat(jsonb.toJson(email)).isEqualTo("\"user@example.com\"");
    }

    @Test
    void emailDeserializesFromFlatString() {
        var email = jsonb.fromJson("\"user@example.com\"", Email.class);
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    // --- Cpf serialization ---

    @Test
    void cpfSerializesToFlatString() {
        var cpf = new Cpf(VALID_CPF);
        assertThat(jsonb.toJson(cpf)).isEqualTo("\"" + VALID_CPF + "\"");
    }

    @Test
    void cpfDeserializesFromFlatString() {
        var cpf = jsonb.fromJson("\"" + VALID_CPF + "\"", Cpf.class);
        assertThat(cpf.value()).isEqualTo(VALID_CPF);
    }

    // --- Cnpj serialization ---

    @Test
    void cnpjSerializesToFlatString() {
        var cnpj = new Cnpj(VALID_CNPJ);
        assertThat(jsonb.toJson(cnpj)).isEqualTo("\"" + VALID_CNPJ + "\"");
    }

    @Test
    void cnpjDeserializesFromFlatString() {
        var cnpj = jsonb.fromJson("\"" + VALID_CNPJ + "\"", Cnpj.class);
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ);
    }

    // --- CpfOrCnpj serialization ---

    @Test
    void cpfOrCnpjWithCpfSerializesToFlatString() {
        CpfOrCnpj taxId = new Cpf(VALID_CPF);
        assertThat(jsonb.toJson(taxId)).isEqualTo("\"" + VALID_CPF + "\"");
    }

    @Test
    void cpfOrCnpjWithCnpjSerializesToFlatString() {
        CpfOrCnpj taxId = new Cnpj(VALID_CNPJ);
        assertThat(jsonb.toJson(taxId)).isEqualTo("\"" + VALID_CNPJ + "\"");
    }

    @Test
    void cpfOrCnpjDeserializesElevenDigitsToCpf() {
        CpfOrCnpj result = jsonb.fromJson("\"" + VALID_CPF + "\"", CpfOrCnpj.class);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfOrCnpjDeserializesFourteenDigitsToCnpj() {
        CpfOrCnpj result = jsonb.fromJson("\"" + VALID_CNPJ + "\"", CpfOrCnpj.class);
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    // --- null handling ---

    @Test
    void jsonNullDeserializesToNullEmail() {
        var result = jsonb.fromJson("null", Email.class);
        assertThat(result).isNull();
    }

    // --- error handling ---

    @Test
    void invalidEmailDuringDeserializationThrowsJsonbException() {
        assertThatThrownBy(() -> jsonb.fromJson("\"not-an-email\"", Email.class))
                .isInstanceOf(JsonbException.class);
    }

    @Test
    void invalidCpfDuringDeserializationThrowsJsonbException() {
        assertThatThrownBy(() -> jsonb.fromJson("\"12345\"", Cpf.class))
                .isInstanceOf(JsonbException.class);
    }

    // --- embedded in a DTO ---

    /**
     * Defined at class level (not inside a method) so Yasson can access
     * the record's accessor methods via reflection.
     */
    public record ContactDto(String name, Email email, CpfOrCnpj taxId) {}

    @Test
    void evoEmbeddedInDtoSerializesFlat() {
        var dto = new ContactDto("Alice", new Email("alice@example.com"), new Cpf(VALID_CPF));
        var json = jsonb.toJson(dto);

        assertThat(json).contains("\"email\":\"alice@example.com\"")
                .contains("\"taxId\":\"" + VALID_CPF + "\"")
                .doesNotContain("{\"value\"");
    }

    @Test
    void evoEmbeddedInDtoDeserializesFromFlat() {
        var json = """
                {"name":"Alice","email":"alice@example.com","taxId":"%s"}
                """.formatted(VALID_CPF);

        var dto = jsonb.fromJson(json, ContactDto.class);

        assertThat(dto.name()).isEqualTo("Alice");
        assertThat(dto.email().value()).isEqualTo("alice@example.com");
        assertThat(dto.taxId()).isInstanceOf(Cpf.class);
        assertThat(dto.taxId().value()).isEqualTo(VALID_CPF);
    }
}
