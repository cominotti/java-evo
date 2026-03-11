// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jackson;

import dev.cominotti.java.evo.country.CountryCode;
import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.net.IpAddress;
import dev.cominotti.java.evo.phone.AreaCode;
import dev.cominotti.java.evo.phone.PhoneNumber;
import dev.cominotti.java.evo.slug.Slug;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import dev.cominotti.java.evo.url.Url;
import dev.cominotti.java.evo.username.Username;
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

    // --- Slug serialization ---

    @Test
    void slugSerializesToFlatString() throws Exception {
        var slug = new Slug("my-slug");
        assertThat(mapper.writeValueAsString(slug)).isEqualTo("\"my-slug\"");
    }

    @Test
    void slugDeserializesFromFlatString() throws Exception {
        var slug = mapper.readValue("\"my-slug\"", Slug.class);
        assertThat(slug.value()).isEqualTo("my-slug");
    }

    // --- Username serialization ---

    @Test
    void usernameSerializesToFlatString() throws Exception {
        var username = new Username("alice");
        assertThat(mapper.writeValueAsString(username)).isEqualTo("\"alice\"");
    }

    @Test
    void usernameDeserializesFromFlatString() throws Exception {
        var username = mapper.readValue("\"alice\"", Username.class);
        assertThat(username.value()).isEqualTo("alice");
    }

    // --- AreaCode serialization ---

    @Test
    void areaCodeSerializesToFlatString() throws Exception {
        var areaCode = new AreaCode("11");
        assertThat(mapper.writeValueAsString(areaCode)).isEqualTo("\"11\"");
    }

    @Test
    void areaCodeDeserializesFromFlatString() throws Exception {
        var areaCode = mapper.readValue("\"11\"", AreaCode.class);
        assertThat(areaCode.value()).isEqualTo("11");
    }

    // --- PhoneNumber serialization ---

    @Test
    void phoneNumberSerializesToFlatString() throws Exception {
        var phone = new PhoneNumber("+5511999887766");
        assertThat(mapper.writeValueAsString(phone)).isEqualTo("\"+5511999887766\"");
    }

    @Test
    void phoneNumberDeserializesFromFlatString() throws Exception {
        var phone = mapper.readValue("\"+5511999887766\"", PhoneNumber.class);
        assertThat(phone.value()).isEqualTo("+5511999887766");
    }

    // --- CountryCode serialization ---

    @Test
    void countryCodeSerializesToFlatString() throws Exception {
        var code = new CountryCode("BR");
        assertThat(mapper.writeValueAsString(code)).isEqualTo("\"BR\"");
    }

    @Test
    void countryCodeDeserializesFromFlatString() throws Exception {
        var code = mapper.readValue("\"BR\"", CountryCode.class);
        assertThat(code.value()).isEqualTo("BR");
    }

    // --- Url serialization ---

    @Test
    void urlSerializesToFlatString() throws Exception {
        var url = new Url("https://example.com");
        assertThat(mapper.writeValueAsString(url)).isEqualTo("\"https://example.com\"");
    }

    @Test
    void urlDeserializesFromFlatString() throws Exception {
        var url = mapper.readValue("\"https://example.com\"", Url.class);
        assertThat(url.value()).isEqualTo("https://example.com");
    }

    // --- IpAddress serialization ---

    @Test
    void ipAddressSerializesToFlatString() throws Exception {
        var ip = new IpAddress("192.168.1.1");
        assertThat(mapper.writeValueAsString(ip)).isEqualTo("\"192.168.1.1\"");
    }

    @Test
    void ipAddressDeserializesFromFlatString() throws Exception {
        var ip = mapper.readValue("\"192.168.1.1\"", IpAddress.class);
        assertThat(ip.value()).isEqualTo("192.168.1.1");
    }

    // --- embedded in a DTO ---

    @Test
    void evoEmbeddedInDtoSerializesFlat() throws Exception {
        record ContactDto(String name, Email email, CpfOrCnpj taxId) {}

        var dto = new ContactDto("Alice", new Email("alice@example.com"), new Cpf(VALID_CPF));
        var json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"email\":\"alice@example.com\"")
                .contains("\"taxId\":\"" + VALID_CPF + "\"")
                .doesNotContain("{\"value\"");
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
