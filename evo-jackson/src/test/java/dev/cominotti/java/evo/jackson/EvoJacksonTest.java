package dev.cominotti.java.evo.jackson;

import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvoJacksonTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(new EvoModule())
                .build();
    }

    // --- Email serialization ---

    @Test
    void emailSerializesToFlatString() throws Exception {
        var email = new Email("user@example.com");
        assertThat(mapper.writeValueAsString(email)).isEqualTo("\"user@example.com\"");
    }

    @Test
    void emailDeserializesFromFlatString() throws Exception {
        var email = mapper.readValue("\"user@example.com\"", Email.class);
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    // --- Cpf serialization ---

    @Test
    void cpfSerializesToFlatString() throws Exception {
        var cpf = new Cpf(VALID_CPF);
        assertThat(mapper.writeValueAsString(cpf)).isEqualTo("\"" + VALID_CPF + "\"");
    }

    @Test
    void cpfDeserializesFromFlatString() throws Exception {
        var cpf = mapper.readValue("\"" + VALID_CPF + "\"", Cpf.class);
        assertThat(cpf.value()).isEqualTo(VALID_CPF);
    }

    // --- Cnpj serialization ---

    @Test
    void cnpjSerializesToFlatString() throws Exception {
        var cnpj = new Cnpj(VALID_CNPJ);
        assertThat(mapper.writeValueAsString(cnpj)).isEqualTo("\"" + VALID_CNPJ + "\"");
    }

    @Test
    void cnpjDeserializesFromFlatString() throws Exception {
        var cnpj = mapper.readValue("\"" + VALID_CNPJ + "\"", Cnpj.class);
        assertThat(cnpj.value()).isEqualTo(VALID_CNPJ);
    }

    // --- CpfOrCnpj serialization ---

    @Test
    void cpfOrCnpjWithCpfSerializesToFlatString() throws Exception {
        CpfOrCnpj taxId = new Cpf(VALID_CPF);
        assertThat(mapper.writeValueAsString(taxId)).isEqualTo("\"" + VALID_CPF + "\"");
    }

    @Test
    void cpfOrCnpjWithCnpjSerializesToFlatString() throws Exception {
        CpfOrCnpj taxId = new Cnpj(VALID_CNPJ);
        assertThat(mapper.writeValueAsString(taxId)).isEqualTo("\"" + VALID_CNPJ + "\"");
    }

    @Test
    void cpfOrCnpjDeserializesElevenDigitsToCpf() throws Exception {
        CpfOrCnpj result = mapper.readValue("\"" + VALID_CPF + "\"", CpfOrCnpj.class);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfOrCnpjDeserializesFourteenDigitsToCnpj() throws Exception {
        CpfOrCnpj result = mapper.readValue("\"" + VALID_CNPJ + "\"", CpfOrCnpj.class);
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    // --- null handling ---

    @Test
    void nullEmailSerializesAsJsonNull() throws Exception {
        assertThat(mapper.writeValueAsString((Email) null)).isEqualTo("null");
    }

    @Test
    void jsonNullDeserializesToNullEmail() throws Exception {
        var result = mapper.readValue("null", Email.class);
        assertThat(result).isNull();
    }

    // --- error handling ---

    @Test
    void invalidEmailDuringDeserializationThrows() {
        assertThatThrownBy(() -> mapper.readValue("\"not-an-email\"", Email.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void invalidCpfDuringDeserializationThrows() {
        assertThatThrownBy(() -> mapper.readValue("\"12345\"", Cpf.class))
                .isInstanceOf(Exception.class);
    }

    // --- embedded in a DTO ---

    @Test
    void evoEmbeddedInDtoSerializesFlat() throws Exception {
        record ContactDto(String name, Email email, CpfOrCnpj taxId) {}

        var dto = new ContactDto("Alice", new Email("alice@example.com"), new Cpf(VALID_CPF));
        var json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"email\":\"alice@example.com\"");
        assertThat(json).contains("\"taxId\":\"" + VALID_CPF + "\"");
        assertThat(json).doesNotContain("{\"value\"");
    }

    @Test
    void evoEmbeddedInDtoDeserializesFromFlat() throws Exception {
        record ContactDto(String name, Email email, CpfOrCnpj taxId) {}

        var json = """
                {"name":"Alice","email":"alice@example.com","taxId":"%s"}
                """.formatted(VALID_CPF);

        var dto = mapper.readValue(json, ContactDto.class);

        assertThat(dto.name()).isEqualTo("Alice");
        assertThat(dto.email().value()).isEqualTo("alice@example.com");
        assertThat(dto.taxId()).isInstanceOf(Cpf.class);
        assertThat(dto.taxId().value()).isEqualTo(VALID_CPF);
    }
}
