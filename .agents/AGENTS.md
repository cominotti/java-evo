# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

Toolchain is managed by `mise` (Java 25, Maven 3.9.12). Run `eval "$(mise activate bash)"` if `mvn` is not on PATH.

```bash
mvn test                                    # run all tests (all modules)
mvn test -pl evo-core                       # run tests in a single module
mvn test -pl evo-core -Dtest=CpfTest        # single test class in a module
mvn test -pl evo-core -Dtest=CpfTest#validCpfCreatesSuccessfully  # single test method
mvn spring-boot:run -pl evo-example         # start example app (H2 console at localhost:8080/h2-console)
```

## Architecture

Multi-module Maven project under `dev.cominotti.java.evo`:

- **`evo-core`** — Domain EVOs (Java Records with `@EvoType`) and validation (`EvoValidation`, `*Rules` classes, custom constraint annotations). Dependencies: Jakarta Validation only. See the `/evo` skill for the full guide on creating and using EVOs.
- **`evo-persistence`** — JPA converters (`autoApply = true`, `StringEvoConverter<T>` base) and the unified `@EvoColumn` annotation with Hibernate `@AttributeBinderType` binder for column metadata. Dependencies: Jakarta Persistence + Hibernate.
- **`evo-jackson`** — `EvoModule` for flat-string JSON serialization/deserialization of `@EvoType` records. Dependencies: Jackson + optional Spring Context (for `@Component` auto-registration).
- **`evo-example`** — Spring Boot example app with `greeting/` package demonstrating EVO integration with entities, controllers, and repositories. Dependencies: all three modules above + Spring Boot.

All commands run from the project root and build all modules. Use `-pl <module>` to target a single module (e.g., `mvn test -pl evo-core`).

### EVO Pattern Summary

EVOs are records with a single `String value` component annotated with Jakarta Validation constraints. They carry `@EvoType` as a marker but **no** `@Embeddable` or `@Column`. Compact constructors delegate to `EvoValidation.validate()` which uses Jakarta Validation's `validateValue()` to evaluate all annotations — including custom constraints like `@NotAllSameDigit`, `@CpfCheckDigit`, `@CnpjCheckDigit`. Jackson flat-string serialization is handled by the generic `EvoModule` (detects `@EvoType` records). **EVOs are self-validating** — `@Valid` on EVO fields is unnecessary (the compact constructor validates before the object exists; during Jackson deserialization, invalid values are rejected immediately as `MismatchedInputException`).

**Persistence architecture** (`evo-persistence`):
- `StringEvoConverter<T>` is the abstract base for `autoApply` JPA converters
- Concrete converters (`EmailConverter`, `CpfConverter`, `CnpjConverter`) use `@Converter(autoApply = true)` — safe because EVOs are not `@Embeddable`
- A unified `@EvoColumn` annotation uses Hibernate's `@AttributeBinderType` to set column name, length, and nullable programmatically
- Column length is either **derived from `@Size(max)`** on the EVO type's `value` field via `EvoColumnBinder.deriveLengthFromEvoType()`, or specified explicitly via `@EvoColumn(length = ...)` for types without `@Size`
- Entity fields use `@EvoColumn(name = "email")` instead of `@Column(name = "email", length = 320)`

**Validation architecture** (`evo-core`, `validation/` subpackage):
- `*Rules` classes hold regexes, **resource bundle keys** (`"{evo.cpf.blank}"`), and algorithmic methods
- Custom constraint annotations have nested `ConstraintValidator` classes that delegate to `*Rules`
- `EvoValidation` caches a `Validator` instance (replaceable via `setValidatorFactory()`)
- `EvoMessages.resolve()` handles direct-throw paths (`parse()`, `CpfOrCnpj.of()`) using the same resource bundle
- Messages live in `src/main/resources/ValidationMessages.properties` — i18n via locale-specific variants (`ValidationMessages_pt_BR.properties`, etc.)

**Sealed interface for union types:** `CpfOrCnpj` is a `sealed interface permits Cpf, Cnpj` persisted via `@EvoColumn(name = "tax_id", length = CnpjRules.DIGIT_COUNT)` + explicit `@Convert(converter = CpfOrCnpjConverter.class)`. The converter must NOT be `autoApply = true` — Hibernate 7 throws "Multiple auto-apply converters matched" when both a supertype and subtype converter have `autoApply = true`. Sealed interfaces also need explicit `length` because `deriveLengthFromEvoType()` reads `@Size` from concrete types only.

**Column nullability:** `@EvoColumn` defaults to **NOT NULL** (`nullable = false`) because most DDD value objects are required. Use `@EvoColumn(name = "email", nullable = true)` for optional fields.

**Error handling architecture** (`evo-example`):
- `EvoExceptionHandler` (`@RestControllerAdvice`) unifies validation errors into RFC 9457 `ProblemDetail` responses with a consistent `"errors"` array of `{field, message}` entries
- EVO deserialization errors: `SingleValueEvoDeserializer` and `CpfOrCnpjDeserializer` chain the original `IllegalArgumentException` as the cause of `MismatchedInputException` via `initCause()`. The handler extracts the field name from `MismatchedInputException.getPath()` and the i18n message from the chained IAE
- Jakarta Validation errors (`MethodArgumentNotValidException`): the same handler maps field errors into the same `ProblemDetail` structure
- Jackson annotations like `@JsonProperty` on EVO fields in DTOs work correctly — property renaming is handled at the container level, independent of `SingleValueEvoDeserializer`

## Jackson 3.0 (Critical)

Spring Boot 4 ships Jackson **3.0.4**. All packages moved:

| Jackson 2.x | Jackson 3.0 |
|---|---|
| `com.fasterxml.jackson.databind.*` | `tools.jackson.databind.*` |
| `com.fasterxml.jackson.core.*` | `tools.jackson.core.*` |
| `JsonSerializer<T>` | `ValueSerializer<T>` |
| `JsonDeserializer<T>` | `ValueDeserializer<T>` |
| `BeanSerializerModifier` | `ValueSerializerModifier` |
| `SerializerProvider` | `SerializationContext` |

Exception: annotations remain in `com.fasterxml.jackson.annotation` (2.x compat).

Build ObjectMapper in tests: `JsonMapper.builder().addModule(new EvoModule()).build()`

## Spring Boot 4 Test Imports

Test slice annotations moved to new packages:

```java
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;      // NOT ...boot.test.autoconfigure
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // NOT ...boot.test.autoconfigure
```

## Code Comment Conventions

Use insightful Javadoc and inline comments **very often** — especially for:
- Non-obvious design decisions (e.g., why `volatile`, why `synchronized`, why a particular guard clause)
- Threading and concurrency considerations (DCL patterns, race conditions, safe publication)
- Resource lifecycle notes (factory leaks, closeable contracts, singleton scope)
- Framework quirks and constraints (Hibernate 7 behaviors, Jakarta Validation conventions, record constructor limitations)
- Algorithmic explanations (check digit formulas, weight arrays, mod-11 logic)

Comments should explain **why**, not **what**. Prioritize documenting the reasoning behind the code so future readers understand the constraints and tradeoffs.

## Test Conventions

- **Unit tests** (no Spring): `EmailTest`, `CpfTest` — plain JUnit 5 + AssertJ
- **Integration tests** (Spring context): `EvoPersistenceIntegrationTest` (`@DataJpaTest`), `EvoControllerIntegrationTest` (`@SpringBootTest`)
- Method names: descriptive camelCase (`validCpfCreatesSuccessfully`, `parseFormattedCnpj`)
- Use `assertThat` (AssertJ), `assertThatThrownBy` for exception testing
- Jakarta Validation annotations on records propagate to **fields**, not record components. Use `getDeclaredField("value").getAnnotation(...)` for reflection-based checks.
