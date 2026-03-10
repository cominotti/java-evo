# java-evo

Enterprise Value Objects (EVOs) for Java — DDD value types as self-validating Records with Jakarta Validation, JPA persistence via `autoApply` converters, and flat-string serialization for both Jackson and JSON-B.

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
| **`evo-core`** | Domain EVOs + validation (`EvoValidation`, `EvoTypes`, `*Rules`, custom constraints) | Jakarta Validation only |
| **`evo-persistence`** | JPA `autoApply` converters + automatic column length derivation | Jakarta Persistence + Hibernate |
| **`evo-jackson`** | `EvoModule` for flat-string JSON serialization (Jackson) | Jackson 3.0 + optional Spring Context |
| **`evo-jsonb`** | `StringEvoJsonbAdapter<T>` for flat-string JSON serialization (JSON-B) | Jakarta JSON Binding |
| **`evo-rest`** | Jakarta REST integration (`ExceptionMapper`, `ParamConverterProvider`) | Jakarta REST + JSON Binding + Validation |
| **`evo-spring-example`** | Spring Boot + Spring MVC example app | evo-core/persistence/jackson + Spring Boot 4 |
| **`evo-jakarta-example`** | Standalone Jakarta REST + JSON-B example app (no Spring) | evo-core/persistence/jsonb/rest + Jersey + Grizzly |

## Key Design Decisions

### Self-validating — no `@Valid` needed

EVOs validate in their compact constructor via `EvoValidation.validate()`. Any EVO instance that exists is guaranteed valid. `@Valid` on EVO entity fields or DTO components is unnecessary — the constructor validates before the object exists.

### Persistence via standard `@Column` + `autoApply` converters

EVOs carry **no** `@Embeddable`. Entity fields use standard JPA `@Column`, with column lengths auto-derived by `EvoColumnMetadataIntegrator`:

```java
@Column(name = "email")                                         // length auto-derived from @Size(max=320)
private Email email;

@Column(name = "author_cpf", length = CpfRules.DIGIT_COUNT, nullable = false)  // explicit length + NOT NULL
private Cpf authorCpf;
```

- **Column length**: auto-derived from `@Size(max)` when the column has JPA default length (255), or set explicitly via `@Column(length = ...)`
- **Column nullability**: standard JPA default (`nullable = true`). Use `@Column(nullable = false)` for required fields
- **Type conversion**: `autoApply` converters — no `@Convert` needed (except for sealed interfaces)

### Dual serialization: Jackson + JSON-B

**Jackson** (`evo-jackson`): `EvoModule` auto-detects `@EvoType` records via `ValueSerializerModifier` — zero configuration needed.

**JSON-B** (`evo-jsonb`): `StringEvoJsonbAdapter<T>` base class with per-type one-liner subclasses. Adapters are auto-discovered via `ServiceLoader`:

```java
// One-liner adapter:
public class PhoneJsonbAdapter extends StringEvoJsonbAdapter<Phone> {
    public PhoneJsonbAdapter() { super(Phone::value, Phone.class); }
}

// Register in META-INF/services/dev.cominotti.java.evo.jsonb.EvoJsonbAdapterProvider

// Auto-discovered — no explicit registration:
var jsonb = JsonbBuilder.create(EvoJsonbConfig.withDefaults(new JsonbConfig()));
```

Both produce the same flat-string JSON: `{"email": "alice@example.com"}`

### Unified error responses

Both Spring MVC and Jakarta REST produce the same error format:

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "Request validation failed with 1 error(s).",
  "errors": [{"field": "email", "message": "Email must be a valid email address"}]
}
```

- **Spring MVC** (`evo-spring-example`): `EvoExceptionHandler` handles `HttpMessageNotReadableException` + `MethodArgumentNotValidException` → `ProblemDetail`
- **Jakarta REST** (`evo-rest`): `EvoJsonbExceptionMapper` handles `ProcessingException` (wrapping `JsonbException`) + `EvoConstraintViolationExceptionMapper` handles `ConstraintViolationException` → `ValidationProblem`

### Sealed interfaces for union types

`CpfOrCnpj` is a `sealed interface permits Cpf, Cnpj` with:
- Explicit `@Convert(converter = CpfOrCnpjConverter.class)` on entity fields (NOT `autoApply`)
- Explicit `@Column(length = ...)` (auto-derivation reads `@Size` from `@EvoType` records only, not sealed interfaces)
- Auto-detection by digit count in both Jackson (`CpfOrCnpj.of()`) and JSON-B (`CpfOrCnpjJsonbAdapter`)

## Stack

- Java 25, Maven 3.9.12 (via [mise](https://mise.jdx.dev/))
- Spring Boot 4.0.3, Hibernate 7.2.4
- Jackson 3.0.4, JSON-B 3.0.1 (Yasson)
- Jersey 4.0.2 (Jakarta REST), Grizzly HTTP server
- Jakarta Validation 3.1, Jakarta Persistence 3.2
- H2 (in-memory, for example apps)

## Quick Start

```bash
# Run all tests (all 7 modules)
mvn test

# Start the Spring MVC example (port 8080)
mvn spring-boot:run -pl evo-spring-example

# Start the Jakarta REST example (port 8081)
mvn exec:java -pl evo-jakarta-example \
  -Dexec.mainClass=dev.cominotti.java.evo.jakarta.example.JakartaEvoApplication

# Create a greeting with an EVO field
curl -X POST http://localhost:8080/greetings \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "message": "Hello!", "email": "alice@example.com"}'
```

## License

Apache-2.0
