// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

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

    // --- Slug serialization ---

    @Test
    void slugSerializesToFlatString() {
        var slug = new Slug("my-slug");
        assertThat(jsonb.toJson(slug)).isEqualTo("\"my-slug\"");
    }

    @Test
    void slugDeserializesFromFlatString() {
        var slug = jsonb.fromJson("\"my-slug\"", Slug.class);
        assertThat(slug.value()).isEqualTo("my-slug");
    }

    // --- Username serialization ---

    @Test
    void usernameSerializesToFlatString() {
        var username = new Username("alice");
        assertThat(jsonb.toJson(username)).isEqualTo("\"alice\"");
    }

    @Test
    void usernameDeserializesFromFlatString() {
        var username = jsonb.fromJson("\"alice\"", Username.class);
        assertThat(username.value()).isEqualTo("alice");
    }

    // --- AreaCode serialization ---

    @Test
    void areaCodeSerializesToFlatString() {
        var areaCode = new AreaCode("11");
        assertThat(jsonb.toJson(areaCode)).isEqualTo("\"11\"");
    }

    @Test
    void areaCodeDeserializesFromFlatString() {
        var areaCode = jsonb.fromJson("\"11\"", AreaCode.class);
        assertThat(areaCode.value()).isEqualTo("11");
    }

    // --- PhoneNumber serialization ---

    @Test
    void phoneNumberSerializesToFlatString() {
        var phone = new PhoneNumber("+5511999887766");
        assertThat(jsonb.toJson(phone)).isEqualTo("\"+5511999887766\"");
    }

    @Test
    void phoneNumberDeserializesFromFlatString() {
        var phone = jsonb.fromJson("\"+5511999887766\"", PhoneNumber.class);
        assertThat(phone.value()).isEqualTo("+5511999887766");
    }

    // --- CountryCode serialization ---

    @Test
    void countryCodeSerializesToFlatString() {
        var code = new CountryCode("BR");
        assertThat(jsonb.toJson(code)).isEqualTo("\"BR\"");
    }

    @Test
    void countryCodeDeserializesFromFlatString() {
        var code = jsonb.fromJson("\"BR\"", CountryCode.class);
        assertThat(code.value()).isEqualTo("BR");
    }

    // --- Url serialization ---

    @Test
    void urlSerializesToFlatString() {
        var url = new Url("https://example.com");
        assertThat(jsonb.toJson(url)).isEqualTo("\"https://example.com\"");
    }

    @Test
    void urlDeserializesFromFlatString() {
        var url = jsonb.fromJson("\"https://example.com\"", Url.class);
        assertThat(url.value()).isEqualTo("https://example.com");
    }

    // --- IpAddress serialization ---

    @Test
    void ipAddressSerializesToFlatString() {
        var ip = new IpAddress("192.168.1.1");
        assertThat(jsonb.toJson(ip)).isEqualTo("\"192.168.1.1\"");
    }

    @Test
    void ipAddressDeserializesFromFlatString() {
        var ip = jsonb.fromJson("\"192.168.1.1\"", IpAddress.class);
        assertThat(ip.value()).isEqualTo("192.168.1.1");
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
