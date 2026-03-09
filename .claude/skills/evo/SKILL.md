---
name: evo
description: Guide for creating, configuring, and using Enterprise Value Objects (EVOs) — DDD value types as Java Records with Jakarta Validation and Jackson support, persisted via autoApply converters and @AttributeBinderType column annotations.
---

# Enterprise Value Objects (EVOs)

EVOs are DDD Value Types implemented as Java Records. They are immutable, self-validating, and work seamlessly with JPA persistence (via `autoApply` converters + `@AttributeBinderType` column annotations), Jakarta Validation, and Jackson serialization. EVOs carry **no** `jakarta.persistence` annotations — persistence concerns are fully decoupled into the `evo-persistence` module.

## When to Use This Skill

Use when:
- Creating a new EVO type (e.g., Phone, Cep, Uuid-based ID)
- Using an EVO in a JPA entity
- Understanding how EVOs serialize to JSON or persist to databases
- Writing tests for EVO types

## Architecture Overview

```
java-evo/                              ← multi-module Maven root
├── evo-core/                          ← domain EVOs + validation (jakarta.validation only)
│   └── dev.cominotti.java.evo/
│       ├── EvoType.java    ← marker annotation
│       ├── Email.java                    ← domain record (no jakarta.persistence)
│       ├── Cpf.java                      ← domain record implements CpfOrCnpj
│       ├── Cnpj.java                     ← domain record implements CpfOrCnpj
│       ├── CpfOrCnpj.java               ← sealed interface (union type)
│       └── validation/
│           ├── EvoValidation.java        ← replaceable Validator holder + validate()
│           ├── TaxIdRules.java           ← shared: mod11Remainder(), CpfOrCnpj messages
│           ├── CpfRules.java             ← CPF: REGEX, messages, hasValidCheckDigits()
│           ├── CnpjRules.java            ← CNPJ: REGEX, messages, hasValidCheckDigits()
│           ├── EmailRules.java           ← Email: MAX_LENGTH, messages
│           ├── NotAllSameDigit.java      ← @NotAllSameDigit + nested ConstraintValidator
│           ├── CpfCheckDigit.java        ← @CpfCheckDigit + nested ConstraintValidator
│           └── CnpjCheckDigit.java       ← @CnpjCheckDigit + nested ConstraintValidator
├── evo-persistence/                   ← JPA converters + @EvoColumn (jakarta.persistence + hibernate)
│   └── dev.cominotti.java.evo.persistence/
│       ├── StringEvoConverter.java       ← abstract autoApply converter base
│       ├── EmailConverter.java           ← @Converter(autoApply = true)
│       ├── CpfConverter.java             ← @Converter(autoApply = true)
│       ├── CnpjConverter.java            ← @Converter(autoApply = true)
│       ├── CpfOrCnpjConverter.java       ← @Converter (NOT autoApply — sealed interface conflict)
│       ├── EvoColumn.java                ← unified @AttributeBinderType annotation for all EVO types
│       └── EvoColumnBinder.java          ← derives length from @Size(max) or uses explicit length
├── evo-jackson/                       ← EvoModule for flat-string JSON ser/des (jackson + optional spring-context)
│   └── dev.cominotti.java.evo.jackson/
│       ├── EvoModule.java                ← Jackson 3.0 Module (auto-registered @Component)
│       ├── SingleValueEvoSerializer.java ← detects @EvoType records
│       └── SingleValueEvoDeserializer.java
└── evo-example/                       ← Spring Boot example app
    └── dev.cominotti.java.evo/
        ├── JavaEvoApplication.java
        └── greeting/                     ← example domain: entities, controllers, repositories
```

**Key design decisions:**
- Java Records with `@EvoType` marker — **no** `@Embeddable` or `@Column`
- **Persistence decoupled**: `autoApply` converters handle type mapping; unified `@EvoColumn` with `@AttributeBinderType` handles column metadata (name, length, nullable)
- **Column length derived or explicit**: `EvoColumnBinder.deriveLengthFromEvoType()` reads `@Size(max)` from the EVO type's `value` field via reflection; for types without `@Size` (e.g., `Cpf`, `Cnpj`), length is specified explicitly via `@EvoColumn(length = ...)`
- **Single validation path**: compact constructor calls `EvoValidation.validate()` which uses `Validator.validateValue()` to evaluate all annotations (built-in + custom)
- **`*Rules` classes** hold regexes, resource bundle keys (`"{evo.cpf.blank}"`), and algorithmic methods — shared between annotations and referenced by custom `ConstraintValidator`s
- **i18n-ready**: all messages in `ValidationMessages.properties`, resolved via Jakarta Validation interpolation or `EvoMessages.resolve()` for non-annotation paths
- **Custom constraint annotations** (`@NotAllSameDigit`, `@CpfCheckDigit`, `@CnpjCheckDigit`) with nested validator classes that delegate to `*Rules`
- **Jackson 3.0** flat-string serialization via generic `ValueSerializerModifier` — any `@EvoType` record with a single `String` component auto-serializes as a bare JSON string
- Column defaults are **nullable** — entities control NOT NULL via `@EvoColumn(nullable = false)`

## Creating a New EVO Type

### Step 1: Create a Rules class (in `evo-core`)

```java
package dev.cominotti.java.evo.validation;

public final class PhoneRules {
    private PhoneRules() {}

    public static final String REGEX = "\\d{10,11}";
    public static final int MAX_LENGTH = 11;

    // Resource bundle keys — resolved from ValidationMessages.properties
    public static final String BLANK_MESSAGE = "{evo.phone.blank}";
    public static final String FORMAT_MESSAGE = "{evo.phone.format}";
    public static final String NULL_MESSAGE = "{evo.phone.null}";
}
```

### Step 2: Define the Record in `evo-core` (no jakarta.persistence)

```java
package dev.cominotti.java.evo;

import dev.cominotti.java.evo.validation.EvoMessages;
import dev.cominotti.java.evo.validation.EvoValidation;
import dev.cominotti.java.evo.validation.PhoneRules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@EvoType
public record Phone(
        @NotBlank(message = PhoneRules.BLANK_MESSAGE)
        @Pattern(regexp = PhoneRules.REGEX, message = PhoneRules.FORMAT_MESSAGE)
        String value
) {

    public Phone {
        EvoValidation.validate(Phone.class, "value", value);
    }

    // Optional: accept formatted input like "(11) 99999-8888"
    public static Phone parse(String formatted) {
        if (formatted == null) {
            throw new IllegalArgumentException(EvoMessages.resolve(PhoneRules.NULL_MESSAGE));
        }
        return new Phone(formatted.replaceAll("\\D", ""));
    }

    @Override
    public String toString() {
        return value;
    }
}
```

### Step 3: No Jackson registration needed

The `EvoModule` (Jackson) auto-discovers any `@EvoType` record with a single `String` component. No manual serializer registration is needed.

### Step 4: Create the persistence converter (in `evo-persistence`)

```java
package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.Phone;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PhoneConverter extends StringEvoConverter<Phone> {
    public PhoneConverter() { super(Phone::value, Phone::new); }
}
```

### Step 5: Use @EvoColumn on entity fields in the consumer project (no new annotation needed)

No new annotation or binder is required. The unified `@EvoColumn` + `EvoColumnBinder` handles
all EVO types. If the EVO type has `@Size(max=N)` on its `value` field, length is derived
automatically. Otherwise, specify `length` explicitly:

```java
// If PhoneRules defines @Size(max = MAX_LENGTH) on Phone.value:
@EvoColumn(name = "phone")                             // length derived from @Size(max)
private Phone phone;

// If Phone does NOT have @Size, specify length explicitly:
@EvoColumn(name = "phone", length = PhoneRules.MAX_LENGTH)
private Phone phone;
```

### Step 6: Write tests

Follow the existing test patterns across modules:
- **Unit test** in `evo-core` (`PhoneTest.java`): valid/invalid construction, `parse()`, equality, toString
- **Converter unit test** in `evo-persistence` (add to `ConverterUnitTest.java`): round-trip, null handling
- **Validation test** in `evo-core` (add to `EvoValidationTest.java`): verify annotations exist
- **Persistence test** in `evo-example` (add to `EvoPersistenceIntegrationTest.java`): use `@EvoColumn` on entity, save/reload
- **Column metadata test** in `evo-example` (add to `AttributeOverrideColumnInheritanceTest.java`): verify column length via `INFORMATION_SCHEMA`

## Configuring Column Names and Constraints

### With @Size-derived length (e.g., Email has @Size(max=320))

```java
@EvoColumn(name = "phone", length = PhoneRules.MAX_LENGTH)   // explicit length=11, nullable=true
private Phone phone;
```

### With explicit length (types without @Size)

```java
// Length derived from @Size(max=320) on Email.value
@EvoColumn(name = "email")
@Valid
private Email email;

// Override column name — length still derived from @Size(max)
@EvoColumn(name = "work_email")
@Valid
private Email workEmail;

// Override column name AND add NOT NULL constraint
@EvoColumn(name = "primary_email", nullable = false)
@Valid
private Email primaryEmail;

// Explicit length for types without @Size
@EvoColumn(name = "author_cpf", length = CpfRules.DIGIT_COUNT, nullable = false)
@Valid
private Cpf authorCpf;
```

## Using EVOs in JPA Entities

### Single-type fields (use @EvoColumn + autoApply converter)

```java
@EvoColumn(name = "email")                                      // length derived from @Size(max=320)
@Valid
private Email email;

@EvoColumn(name = "author_cpf", length = CpfRules.DIGIT_COUNT)  // explicit length=11
@Valid
private Cpf authorCpf;
```

No `@Convert` needed — `autoApply = true` converters handle it automatically.

### Union-type fields (use @EvoColumn + explicit @Convert)

For sealed interface types like `CpfOrCnpj`, explicit `@Convert` is required because
`autoApply` on the interface converter conflicts with subtype converters. Explicit `length` is
also required since `deriveLengthFromEvoType()` reads `@Size` from concrete types only:

```java
@EvoColumn(name = "tax_id", length = CnpjRules.DIGIT_COUNT)
@Convert(converter = CpfOrCnpjConverter.class)
private CpfOrCnpj taxId;
```

**Important:** `CpfOrCnpjConverter` must NOT be `autoApply = true` — Hibernate 7 throws "Multiple auto-apply converters matched" when both a supertype and subtype converter have `autoApply = true`.

## Using EVOs in DTOs and Controllers

### Request/response records

```java
public record CreateUserRequest(
    @NotBlank String name,
    @Valid Email email,         // validates Email annotations via cascading
    @Valid CpfOrCnpj taxId     // auto-detects CPF/CNPJ from JSON string
) {}
```

### JSON format

With the `EvoModule` active (auto-registered via `@Component`), EVOs serialize as flat strings:

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "taxId": "52998224725"
}
```

Without the module, they would serialize as objects: `{"email": {"value": "alice@example.com"}}`.

### Accepting formatted input

Use `parse()` to accept punctuated input from users:

```java
var cpf = Cpf.parse("529.982.247-25");        // → Cpf("52998224725")
var cnpj = Cnpj.parse("11.222.333/0001-81");  // → Cnpj("11222333000181")
var taxId = CpfOrCnpj.parse("529.982.247-25"); // → Cpf("52998224725")
```

## Testing EVOs

### Unit test pattern (no Spring context)

```java
class PhoneTest {
    @Test
    void validPhoneCreatesSuccessfully() {
        var phone = new Phone("11999998888");
        assertThat(phone.value()).isEqualTo("11999998888");
    }

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Phone(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseFormattedPhone() {
        var phone = Phone.parse("(11) 99999-8888");
        assertThat(phone.value()).isEqualTo("11999998888");
    }
}
```

### Jakarta Validation test (plain JUnit — no Spring context)

```java
class EvoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validPhoneHasNoViolations() {
        assertThat(validator.validate(new Phone("11999998888"))).isEmpty();
    }
}
```

### Persistence integration test

```java
@DataJpaTest
class EvoPersistenceIntegrationTest {
    @Autowired GreetingRepository repository;

    @Test
    void saveAndReloadWithPhone() {
        var greeting = new Greeting("Alice", "Hello!");
        greeting.setPhone(new Phone("11999998888"));
        var saved = repository.save(greeting);
        repository.flush();
        var found = repository.findById(saved.getId());
        assertThat(found.get().getPhone().value()).isEqualTo("11999998888");
    }
}
```

## Reference: Existing EVO Types

| Type | Package | Validation | Column Default | Notes |
|---|---|---|---|---|
| `Email` | `evo` | `@NotBlank @Email @Size(max=320)` — constants in `EmailRules` | `email VARCHAR(320)` | RFC 5321 max length |
| `Cpf` | `evo` | `@NotBlank @Pattern @NotAllSameDigit @CpfCheckDigit` — constants in `CpfRules` | `cpf VARCHAR(11)` | Brazilian individual tax ID |
| `Cnpj` | `evo` | `@NotBlank @Pattern @NotAllSameDigit @CnpjCheckDigit` — constants in `CnpjRules` | `cnpj VARCHAR(14)` | Brazilian company tax ID |
| `CpfOrCnpj` | `evo` | Sealed interface — delegates to Cpf/Cnpj — messages in `TaxIdRules` | via `@Convert` | Union type with `of()` and `parse()` |

## Technical Constraints

- **Jackson 3.0** (Spring Boot 4): packages are `tools.jackson.*`, NOT `com.fasterxml.jackson.*`
- **Hibernate 7.2.4**: `@AttributeBinderType` + `autoApply` converters compose correctly (verified)
- Column defaults should be **nullable** — use `@EvoColumn(nullable = false)` at entity level for NOT NULL
- `CpfOrCnpjConverter` must use explicit `@Convert`, NOT `autoApply = true`
- Without `spring-boot-starter-parent`, add `<maven.compiler.parameters>true</maven.compiler.parameters>` to root POM

## Java Records Annotation Propagation

When you annotate a record component, Java propagates the annotation to multiple targets (field, constructor parameter, accessor method) — but ONLY if the annotation's `@Target` includes that element type.

**Key implication for testing:** Jakarta Validation annotations like `@NotBlank`, `@Pattern`, `@Email` have `@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})` — they do **NOT** include `RECORD_COMPONENT`. This means:

- `RecordComponent.getAnnotation(NotBlank.class)` returns **null**
- `field.getAnnotation(NotBlank.class)` returns the annotation (via propagation to `FIELD`)
- Constructor parameter annotations also receive the propagation

When writing reflection-based tests to verify annotation metadata on EVO records, use the **declared field**, not the record component:

```java
// WRONG — returns null for Jakarta Validation annotations
recordClass.getRecordComponents()[0].getAnnotation(NotBlank.class);

// CORRECT — annotations propagate to the field
recordClass.getDeclaredField("value").getAnnotation(NotBlank.class);
```

Only annotations with `@Target(RECORD_COMPONENT)` (like some custom annotations) are available via `RecordComponent.getAnnotation()`.
