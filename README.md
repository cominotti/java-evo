# java-evo

Enterprise Value Objects (EVOs) for Java — DDD value types as self-validating Records with Jakarta Validation, JPA persistence via `autoApply` converters, and Jackson flat-string serialization.

## What are EVOs?

EVOs are immutable, self-validating Java Records that represent domain concepts like `Email`, `Cpf`, and `Cnpj`. They enforce their own invariants at construction time — it is impossible to create an invalid EVO instance.

```java
@EvoType
public record Email(
        @NotBlank(message = EmailRules.BLANK_MESSAGE)
        @jakarta.validation.constraints.Email(message = EmailRules.FORMAT_MESSAGE)
        @Size(max = EmailRules.MAX_LENGTH, message = EmailRules.MAX_LENGTH_MESSAGE)
        String value
) {
    public Email {
        EvoValidation.validate(Email.class, "value", value);
    }
}
```

## Modules

| Module | Purpose | Dependencies |
|---|---|---|
| **`evo-core`** | Domain EVOs + validation (`EvoValidation`, `*Rules`, custom constraints) | Jakarta Validation only |
| **`evo-persistence`** | JPA `autoApply` converters + unified `@EvoColumn` annotation | Jakarta Persistence + Hibernate |
| **`evo-jackson`** | `EvoModule` for flat-string JSON serialization/deserialization | Jackson 3.0 + optional Spring Context |
| **`evo-example`** | Spring Boot example app demonstrating full EVO integration | All modules + Spring Boot 4 |

## Key Design Decisions

### Self-validating — no `@Valid` needed

EVOs validate in their compact constructor via `EvoValidation.validate()`. Any EVO instance that exists is guaranteed valid. `@Valid` on EVO entity fields or DTO components is unnecessary — the constructor validates before the object exists.

During Jackson deserialization, invalid values are rejected immediately by the constructor. The `EvoExceptionHandler` in `evo-example` surfaces the i18n validation message as a clean `ProblemDetail` (RFC 9457) response.

### Persistence via `@EvoColumn` + `autoApply` converters

EVOs carry **no** `@Embeddable` or `@Column`. Persistence is fully decoupled:

```java
// Entity field — length derived from @Size(max=320) on Email.value
@EvoColumn(name = "email", nullable = true)
private Email email;

// Explicit length for types without @Size
@EvoColumn(name = "author_cpf", length = CpfRules.DIGIT_COUNT)
private Cpf authorCpf;
```

- **Column length**: derived automatically from `@Size(max)` on the EVO's `value` field, or set explicitly via `@EvoColumn(length = ...)`
- **Column nullability**: defaults to **NOT NULL** (`nullable = false`). Use `nullable = true` for optional fields
- **Type conversion**: handled by `autoApply` converters (`EmailConverter`, `CpfConverter`, `CnpjConverter`) — no `@Convert` annotation needed on entity fields (except for sealed interfaces like `CpfOrCnpj`)

### Flat-string JSON serialization

With the `EvoModule` registered (auto-registered as `@Component` in Spring), EVOs serialize as bare JSON strings:

```json
{"email": "alice@example.com", "authorCpf": "52998224725"}
```

Jackson annotations like `@JsonProperty` on EVO fields in DTOs work correctly:

```java
public record CreateUserRequest(
    @JsonProperty("contact_email") Email email   // JSON uses "contact_email"
) {}
```

### Unified error responses

`EvoExceptionHandler` (`@RestControllerAdvice` in `evo-example`) produces consistent RFC 9457 `ProblemDetail` responses for both EVO deserialization errors and Jakarta Validation errors:

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "Request validation failed with 1 error(s).",
  "errors": [
    {"field": "email", "message": "Email must be a valid email address"}
  ]
}
```

Nested EVO fields report full dotted paths (e.g., `contact.address.email`).

### Sealed interfaces for union types

`CpfOrCnpj` is a `sealed interface permits Cpf, Cnpj` with:
- Explicit `@Convert(converter = CpfOrCnpjConverter.class)` on entity fields (NOT `autoApply` — Hibernate 7 throws "Multiple auto-apply converters matched" when both supertype and subtype converters use `autoApply`)
- Explicit `@EvoColumn(length = ...)` (length derivation reads `@Size` from concrete types only)
- Auto-detection in Jackson (`CpfOrCnpj.of(value)` detects CPF vs CNPJ by digit count)

## Stack

- Java 25, Maven 3.9.12 (via [mise](https://mise.jdx.dev/))
- Spring Boot 4.0.3, Hibernate 7.2.4
- Jackson 3.0.4 (`tools.jackson.*` packages, annotations still in `com.fasterxml.jackson.annotation`)
- Jakarta Validation 3.1, Jakarta Persistence 3.2
- H2 (in-memory, for the example app)

## Quick Start

```bash
# Run all tests
mvn test

# Start the example app (H2 console at localhost:8080/h2-console)
mvn spring-boot:run -pl evo-example

# Create a greeting with an EVO field
curl -X POST http://localhost:8080/greetings \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "message": "Hello!", "email": "alice@example.com"}'
```

## License

Apache-2.0
