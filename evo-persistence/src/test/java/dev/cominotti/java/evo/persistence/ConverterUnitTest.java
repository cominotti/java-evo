// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.persistence;

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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EVO converters — no Spring context required.
 *
 * <p>Each converter extends {@link StringEvoConverter} and must handle null in both
 * directions, and round-trip correctly for valid values. Invalid values are rejected
 * by the EVO compact constructor (which throws {@code IllegalArgumentException}),
 * so converter tests only cover valid inputs and null.</p>
 */
class ConverterUnitTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_SLUG = "my-slug";
    private static final String VALID_USERNAME = "alice";
    private static final String VALID_AREA_CODE = "11";
    private static final String VALID_PHONE = "+5511999887766";
    private static final String VALID_COUNTRY = "BR";
    private static final String VALID_URL = "https://example.com";
    private static final String VALID_IP = "192.168.1.1";

    // --- EmailConverter ---

    @Test
    void emailConverterToDatabase() {
        var converter = new EmailConverter();
        assertThat(converter.convertToDatabaseColumn(new Email(VALID_EMAIL))).isEqualTo(VALID_EMAIL);
    }

    @Test
    void emailConverterFromDatabase() {
        var converter = new EmailConverter();
        assertThat(converter.convertToEntityAttribute(VALID_EMAIL).value()).isEqualTo(VALID_EMAIL);
    }

    @Test
    void emailConverterNullBothDirections() {
        var converter = new EmailConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CpfConverter ---

    @Test
    void cpfConverterRoundTrip() {
        var converter = new CpfConverter();
        var cpf = new Cpf(VALID_CPF);
        var dbValue = converter.convertToDatabaseColumn(cpf);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfConverterNullBothDirections() {
        var converter = new CpfConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CnpjConverter ---

    @Test
    void cnpjConverterRoundTrip() {
        var converter = new CnpjConverter();
        var cnpj = new Cnpj(VALID_CNPJ);
        var dbValue = converter.convertToDatabaseColumn(cnpj);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void cnpjConverterNullBothDirections() {
        var converter = new CnpjConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CpfOrCnpjConverter ---

    @Test
    void cpfOrCnpjConverterDispatchesByCpfLength() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj result = converter.convertToEntityAttribute(VALID_CPF);
        assertThat(result).isInstanceOf(Cpf.class);
        assertThat(result.value()).isEqualTo(VALID_CPF);
    }

    @Test
    void cpfOrCnpjConverterDispatchesByCnpjLength() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj result = converter.convertToEntityAttribute(VALID_CNPJ);
        assertThat(result).isInstanceOf(Cnpj.class);
        assertThat(result.value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void cpfOrCnpjConverterNullBothDirections() {
        var converter = new CpfOrCnpjConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void cpfOrCnpjConverterRoundTripWithCpf() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj original = new Cpf(VALID_CPF);
        var dbValue = converter.convertToDatabaseColumn(original);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isInstanceOf(Cpf.class);
        assertThat(restored.value()).isEqualTo(original.value());
    }

    @Test
    void cpfOrCnpjConverterRoundTripWithCnpj() {
        var converter = new CpfOrCnpjConverter();
        CpfOrCnpj original = new Cnpj(VALID_CNPJ);
        var dbValue = converter.convertToDatabaseColumn(original);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isInstanceOf(Cnpj.class);
        assertThat(restored.value()).isEqualTo(original.value());
    }

    // --- SlugConverter ---

    @Test
    void slugConverterRoundTrip() {
        var converter = new SlugConverter();
        var slug = new Slug(VALID_SLUG);
        var dbValue = converter.convertToDatabaseColumn(slug);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_SLUG);
    }

    @Test
    void slugConverterNullBothDirections() {
        var converter = new SlugConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- UsernameConverter ---

    @Test
    void usernameConverterRoundTrip() {
        var converter = new UsernameConverter();
        var username = new Username(VALID_USERNAME);
        var dbValue = converter.convertToDatabaseColumn(username);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_USERNAME);
    }

    @Test
    void usernameConverterNullBothDirections() {
        var converter = new UsernameConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- AreaCodeConverter ---

    @Test
    void areaCodeConverterRoundTrip() {
        var converter = new AreaCodeConverter();
        var areaCode = new AreaCode(VALID_AREA_CODE);
        var dbValue = converter.convertToDatabaseColumn(areaCode);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_AREA_CODE);
    }

    @Test
    void areaCodeConverterNullBothDirections() {
        var converter = new AreaCodeConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- PhoneNumberConverter ---

    @Test
    void phoneNumberConverterRoundTrip() {
        var converter = new PhoneNumberConverter();
        var phone = new PhoneNumber(VALID_PHONE);
        var dbValue = converter.convertToDatabaseColumn(phone);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_PHONE);
    }

    @Test
    void phoneNumberConverterNullBothDirections() {
        var converter = new PhoneNumberConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- CountryCodeConverter ---

    @Test
    void countryCodeConverterRoundTrip() {
        var converter = new CountryCodeConverter();
        var code = new CountryCode(VALID_COUNTRY);
        var dbValue = converter.convertToDatabaseColumn(code);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_COUNTRY);
    }

    @Test
    void countryCodeConverterNullBothDirections() {
        var converter = new CountryCodeConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- UrlConverter ---

    @Test
    void urlConverterRoundTrip() {
        var converter = new UrlConverter();
        var url = new Url(VALID_URL);
        var dbValue = converter.convertToDatabaseColumn(url);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_URL);
    }

    @Test
    void urlConverterNullBothDirections() {
        var converter = new UrlConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    // --- IpAddressConverter ---

    @Test
    void ipAddressConverterRoundTrip() {
        var converter = new IpAddressConverter();
        var ip = new IpAddress(VALID_IP);
        var dbValue = converter.convertToDatabaseColumn(ip);
        var restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored.value()).isEqualTo(VALID_IP);
    }

    @Test
    void ipAddressConverterNullBothDirections() {
        var converter = new IpAddressConverter();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
