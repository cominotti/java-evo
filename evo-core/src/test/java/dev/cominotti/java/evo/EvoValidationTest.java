// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo;

import java.lang.annotation.Annotation;
import java.util.Set;

import dev.cominotti.java.evo.country.CountryCode;
import dev.cominotti.java.evo.country.CountryCodeRules;
import dev.cominotti.java.evo.country.ValidCountryCode;
import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.net.IpAddress;
import dev.cominotti.java.evo.net.IpAddressRules;
import dev.cominotti.java.evo.net.ValidIpAddress;
import dev.cominotti.java.evo.phone.AreaCode;
import dev.cominotti.java.evo.phone.AreaCodeRules;
import dev.cominotti.java.evo.phone.PhoneNumber;
import dev.cominotti.java.evo.phone.PhoneNumberRules;
import dev.cominotti.java.evo.slug.Slug;
import dev.cominotti.java.evo.slug.SlugRules;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.CnpjCheckDigit;
import dev.cominotti.java.evo.taxid.CnpjRules;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfCheckDigit;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import dev.cominotti.java.evo.taxid.CpfRules;
import dev.cominotti.java.evo.taxid.NotAllSameDigit;
import dev.cominotti.java.evo.url.Url;
import dev.cominotti.java.evo.url.UrlRules;
import dev.cominotti.java.evo.url.ValidUrl;
import dev.cominotti.java.evo.username.Username;
import dev.cominotti.java.evo.username.UsernameRules;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates EVO types using a plain Jakarta Validation {@link Validator} — no Spring context
 * required. The validator is obtained from {@link Validation#buildDefaultValidatorFactory()},
 * which discovers Hibernate Validator on the test classpath.
 */
class EvoValidationTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // --- Direct EVO validation: valid instances produce no violations ---

    @Test
    void validEmailHasNoViolations() {
        var email = new Email("user@example.com");
        Set<ConstraintViolation<Email>> violations = validator.validate(email);
        assertThat(violations).isEmpty();
    }

    @Test
    void validCpfHasNoViolations() {
        var cpf = new Cpf(VALID_CPF);
        Set<ConstraintViolation<Cpf>> violations = validator.validate(cpf);
        assertThat(violations).isEmpty();
    }

    @Test
    void validCnpjHasNoViolations() {
        var cnpj = new Cnpj(VALID_CNPJ);
        Set<ConstraintViolation<Cnpj>> violations = validator.validate(cnpj);
        assertThat(violations).isEmpty();
    }

    // --- Annotation metadata verification ---
    // Since compact constructors prevent creating invalid EVOs, we verify
    // that the correct Jakarta Validation annotations are declared on record
    // components. This ensures the annotations will work in framework contexts
    // (e.g., Jackson deserialization + @Valid before constructor invocation).

    @Test
    void emailHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Email.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void emailHasEmailAnnotation() {
        assertThat(hasAnnotation(Email.class, "value", jakarta.validation.constraints.Email.class)).isTrue();
    }

    @Test
    void emailHasSizeAnnotation() {
        assertThat(hasAnnotation(Email.class, "value", Size.class)).isTrue();
    }

    @Test
    void cpfHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Cpf.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void cpfHasPatternAnnotation() {
        var pattern = getAnnotation(Cpf.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(CpfRules.REGEX);
    }

    @Test
    void cpfHasNotAllSameDigitAnnotation() {
        assertThat(hasAnnotation(Cpf.class, "value", NotAllSameDigit.class)).isTrue();
    }

    @Test
    void cpfHasCpfCheckDigitAnnotation() {
        assertThat(hasAnnotation(Cpf.class, "value", CpfCheckDigit.class)).isTrue();
    }

    @Test
    void cnpjHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Cnpj.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void cnpjHasPatternAnnotation() {
        var pattern = getAnnotation(Cnpj.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(CnpjRules.REGEX);
    }

    @Test
    void cnpjHasNotAllSameDigitAnnotation() {
        assertThat(hasAnnotation(Cnpj.class, "value", NotAllSameDigit.class)).isTrue();
    }

    @Test
    void cnpjHasCnpjCheckDigitAnnotation() {
        assertThat(hasAnnotation(Cnpj.class, "value", CnpjCheckDigit.class)).isTrue();
    }

    // --- Slug annotation metadata ---

    @Test
    void slugHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Slug.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void slugHasPatternAnnotation() {
        var pattern = getAnnotation(Slug.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(SlugRules.REGEX);
    }

    @Test
    void slugHasSizeAnnotation() {
        assertThat(hasAnnotation(Slug.class, "value", Size.class)).isTrue();
    }

    @Test
    void validSlugHasNoViolations() {
        var slug = new Slug("my-slug");
        Set<ConstraintViolation<Slug>> violations = validator.validate(slug);
        assertThat(violations).isEmpty();
    }

    // --- Username annotation metadata ---

    @Test
    void usernameHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Username.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void usernameHasPatternAnnotation() {
        var pattern = getAnnotation(Username.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(UsernameRules.REGEX);
    }

    @Test
    void usernameHasSizeAnnotation() {
        assertThat(hasAnnotation(Username.class, "value", Size.class)).isTrue();
    }

    @Test
    void validUsernameHasNoViolations() {
        var username = new Username("alice");
        Set<ConstraintViolation<Username>> violations = validator.validate(username);
        assertThat(violations).isEmpty();
    }

    // --- AreaCode annotation metadata ---

    @Test
    void areaCodeHasNotBlankAnnotation() {
        assertThat(hasAnnotation(AreaCode.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void areaCodeHasPatternAnnotation() {
        var pattern = getAnnotation(AreaCode.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(AreaCodeRules.REGEX);
    }

    @Test
    void validAreaCodeHasNoViolations() {
        var areaCode = new AreaCode("11");
        Set<ConstraintViolation<AreaCode>> violations = validator.validate(areaCode);
        assertThat(violations).isEmpty();
    }

    // --- PhoneNumber annotation metadata ---

    @Test
    void phoneNumberHasNotBlankAnnotation() {
        assertThat(hasAnnotation(PhoneNumber.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void phoneNumberHasPatternAnnotation() {
        var pattern = getAnnotation(PhoneNumber.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(PhoneNumberRules.REGEX);
    }

    @Test
    void validPhoneNumberHasNoViolations() {
        var phone = new PhoneNumber("+5511999887766");
        Set<ConstraintViolation<PhoneNumber>> violations = validator.validate(phone);
        assertThat(violations).isEmpty();
    }

    // --- CountryCode annotation metadata ---

    @Test
    void countryCodeHasNotBlankAnnotation() {
        assertThat(hasAnnotation(CountryCode.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void countryCodeHasPatternAnnotation() {
        var pattern = getAnnotation(CountryCode.class, "value", Pattern.class);
        assertThat(pattern).isNotNull();
        assertThat(pattern.regexp()).isEqualTo(CountryCodeRules.REGEX);
    }

    @Test
    void countryCodeHasValidCountryCodeAnnotation() {
        assertThat(hasAnnotation(CountryCode.class, "value", ValidCountryCode.class)).isTrue();
    }

    @Test
    void validCountryCodeHasNoViolations() {
        var code = new CountryCode("BR");
        Set<ConstraintViolation<CountryCode>> violations = validator.validate(code);
        assertThat(violations).isEmpty();
    }

    // --- Url annotation metadata ---

    @Test
    void urlHasNotBlankAnnotation() {
        assertThat(hasAnnotation(Url.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void urlHasSizeAnnotation() {
        assertThat(hasAnnotation(Url.class, "value", Size.class)).isTrue();
    }

    @Test
    void urlHasValidUrlAnnotation() {
        assertThat(hasAnnotation(Url.class, "value", ValidUrl.class)).isTrue();
    }

    @Test
    void validUrlHasNoViolations() {
        var url = new Url("https://example.com");
        Set<ConstraintViolation<Url>> violations = validator.validate(url);
        assertThat(violations).isEmpty();
    }

    // --- IpAddress annotation metadata ---

    @Test
    void ipAddressHasNotBlankAnnotation() {
        assertThat(hasAnnotation(IpAddress.class, "value", NotBlank.class)).isTrue();
    }

    @Test
    void ipAddressHasSizeAnnotation() {
        assertThat(hasAnnotation(IpAddress.class, "value", Size.class)).isTrue();
    }

    @Test
    void ipAddressHasValidIpAddressAnnotation() {
        assertThat(hasAnnotation(IpAddress.class, "value", ValidIpAddress.class)).isTrue();
    }

    @Test
    void validIpAddressHasNoViolations() {
        var ip = new IpAddress("192.168.1.1");
        Set<ConstraintViolation<IpAddress>> violations = validator.validate(ip);
        assertThat(violations).isEmpty();
    }

    // --- @EvoType marker ---

    @Test
    void allEvoTypesAreMarkedAsEvoTypes() {
        assertThat(Email.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Cpf.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Cnpj.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(CpfOrCnpj.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Slug.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Username.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(AreaCode.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(PhoneNumber.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(CountryCode.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(Url.class.isAnnotationPresent(EvoType.class)).isTrue();
        assertThat(IpAddress.class.isAnnotationPresent(EvoType.class)).isTrue();
    }

    private static boolean hasAnnotation(Class<? extends Record> recordClass,
                                         String componentName,
                                         Class<? extends Annotation> annotationType) {
        return getAnnotation(recordClass, componentName, annotationType) != null;
    }

    private static <A extends Annotation> A getAnnotation(Class<? extends Record> recordClass,
                                                           String componentName,
                                                           Class<A> annotationType) {
        // Jakarta Validation annotations don't target RECORD_COMPONENT,
        // so they propagate to the field and constructor parameter instead.
        // Look up the annotation on the declared field.
        try {
            var field = recordClass.getDeclaredField(componentName);
            return field.getAnnotation(annotationType);
        } catch (NoSuchFieldException _) {
            return null;
        }
    }
}
