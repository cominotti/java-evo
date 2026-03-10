# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

Toolchain is managed by `mise` (Java 25, Maven 3.9.12). Run `eval "$(mise activate bash)"` if `mvn` is not on PATH.

```bash
mvn test                                    # run all tests (all modules)
mvn test -pl evo-core                       # run tests in a single module
mvn test -pl evo-core -Dtest=CpfTest        # single test class in a module
mvn test -pl evo-core -Dtest=CpfTest#validCpfCreatesSuccessfully  # single test method
mvn spring-boot:run -pl evo-spring-example   # start Spring MVC example (H2 console at localhost:8080/h2-console)
make license-check                          # verify Apache-2.0 SPDX headers (runs mvn validate)
make license-fix                            # auto-apply missing SPDX headers
make sonar-local                            # SonarCloud analysis + quality gate + issue table (requires SONAR_TOKEN)
```

## Architecture

Multi-module Maven project under `dev.cominotti.java.evo`:

- **`evo-core`** — Domain EVOs (Java Records with `@EvoType`) and validation. Organized into subpackages: `email/` (Email + EmailRules), `taxid/` (Cpf, Cnpj, CpfOrCnpj + all tax ID rules and constraints), `validation/` (shared EvoValidation + EvoMessages). Shared infrastructure (`EvoType`, `EvoTypes`) stays in the root package. Dependencies: Jakarta Validation only. See the `/evo` skill for the full guide on creating and using EVOs.
- **`evo-persistence`** — JPA converters (`autoApply = true`, `StringEvoConverter<T>` base) and `EvoColumnMetadataIntegrator` (Hibernate `Integrator` that auto-derives column lengths from `@Size(max)` on EVO types). Dependencies: Jakarta Persistence + Hibernate.
- **`evo-jackson`** — `EvoModule` for flat-string JSON serialization/deserialization of `@EvoType` records. Dependencies: Jackson + optional Spring Context (for `@Component` auto-registration).
- **`evo-jsonb`** — `StringEvoJsonbAdapter<T>` for flat-string JSON-B serialization. Per-type adapters auto-discovered via `ServiceLoader` (`EvoJsonbAdapterProvider`). Dependencies: Jakarta JSON Binding.
- **`evo-rest`** — Jakarta REST integration: `EvoJsonbExceptionMapper` (`ExceptionMapper<ProcessingException>`), `EvoConstraintViolationExceptionMapper`, `EvoParamConverterProvider`. Dependencies: Jakarta REST + JSON Binding + Validation.
- **`evo-spring-example`** — Spring Boot + Spring MVC example app with `greeting/` package. Dependencies: evo-core/persistence/jackson + Spring Boot.
- **`evo-jakarta-example`** — Standalone Jakarta REST + JSON-B example app (no Spring). Jersey + Grizzly + JPA/H2. Dependencies: evo-core/persistence/jsonb/rest + Jersey.

All commands run from the project root and build all modules. Use `-pl <module>` to target a single module (e.g., `mvn test -pl evo-core`).

### EVO Pattern Summary

EVOs are records with a single `String value` component annotated with Jakarta Validation constraints. They carry `@EvoType` as a marker but **no** `@Embeddable` or `@Column`. Compact constructors delegate to `EvoValidation.validate()` which uses Jakarta Validation's `validateValue()` to evaluate all annotations — including custom constraints like `@NotAllSameDigit`, `@CpfCheckDigit`, `@CnpjCheckDigit`. Jackson flat-string serialization is handled by the generic `EvoModule` (detects `@EvoType` records). **EVOs are self-validating** — `@Valid` on EVO fields is unnecessary (the compact constructor validates before the object exists; during Jackson deserialization, invalid values are rejected immediately as `MismatchedInputException`).

**Persistence architecture** (`evo-persistence`):
- `StringEvoConverter<T>` is the abstract base for `autoApply` JPA converters
- Concrete converters (`EmailConverter`, `CpfConverter`, `CnpjConverter`) use `@Converter(autoApply = true)` — safe because EVOs are not `@Embeddable`
- `EvoColumnMetadataIntegrator` (Hibernate `Integrator`) auto-derives column length from `@Size(max)` on the EVO type's `value` field — override rule: if `@Column(length = N)` where N ≠ 255, the explicit value wins
- Auto-discovered via `META-INF/services/org.hibernate.integrator.spi.Integrator` — zero configuration needed
- Entity fields use standard `@Column(name = "email")` — length auto-derived from `@Size(max=320)`. For types without `@Size`, use `@Column(name = "cpf", length = 11)` explicitly

**Validation architecture** (`evo-core`):
- Each EVO type's `*Rules` class and custom constraints are co-located in the same subpackage as the EVO record: `email/EmailRules`, `taxid/CpfRules`, `taxid/CnpjRules`, `taxid/TaxIdRules`, etc.
- `*Rules` classes hold regexes, **resource bundle keys** (`"{evo.cpf.blank}"`), and algorithmic methods
- Custom constraint annotations (`taxid/NotAllSameDigit`, `taxid/CpfCheckDigit`, `taxid/CnpjCheckDigit`) have nested `ConstraintValidator` classes that delegate to `*Rules`
- Shared infrastructure stays in `validation/`: `EvoValidation` caches a `Validator` instance (replaceable via `setValidatorFactory()`), `EvoMessages.resolve()` handles direct-throw paths (`parse()`, `CpfOrCnpj.of()`) using the same resource bundle
- Messages live in `src/main/resources/ValidationMessages.properties` — i18n via locale-specific variants (`ValidationMessages_pt_BR.properties`, etc.)

**Sealed interface for union types:** `CpfOrCnpj` is a `sealed interface permits Cpf, Cnpj` persisted via `@Column(name = "tax_id", length = CnpjRules.DIGIT_COUNT)` + explicit `@Convert(converter = CpfOrCnpjConverter.class)`. The converter must NOT be `autoApply = true` — Hibernate 7 throws "Multiple auto-apply converters matched" when both a supertype and subtype converter have `autoApply = true`. Sealed interfaces need explicit `length` because `EvoColumnMetadataIntegrator` only reads `@Size` from single-String `@EvoType` records, not from sealed interfaces.

**Column nullability:** Standard JPA `@Column` defaults apply (`nullable = true`). Use `@Column(name = "email", nullable = false)` for required fields.

**Error handling architecture:**
- **Spring MVC** (`evo-spring-example`): `EvoExceptionHandler` (`@RestControllerAdvice`) unifies `HttpMessageNotReadableException` (EVO errors) and `MethodArgumentNotValidException` (Jakarta Validation) into RFC 9457 `ProblemDetail` with `{field, message}` errors array. Field names from `MismatchedInputException.getPath()` support nested dotted paths
- **Jakarta REST** (`evo-rest`): `EvoJsonbExceptionMapper` (`ExceptionMapper<ProcessingException>`) handles EVO errors — Jersey wraps `JsonbException` in `ProcessingException`, so the mapper walks the cause chain for `JsonbException` → `IllegalArgumentException`. Field name extracted via regex from Yasson's message. `EvoConstraintViolationExceptionMapper` handles `ConstraintViolationException`
- Both produce the same response format: `{"title": "Validation failed", "errors": [{field, message}]}`
- Jackson `@JsonProperty` / JSON-B `@JsonbProperty` on EVO fields in DTOs work correctly — property renaming at the container level is independent of EVO serializers

**JSON-B adapter registration** (`evo-jsonb`):
- `StringEvoJsonbAdapter<T>` base class — per-type one-liner subclasses (mirrors `StringEvoConverter<T>` pattern)
- Adapters implement `EvoJsonbAdapterProvider` and register via `META-INF/services` for `ServiceLoader` auto-discovery
- `EvoJsonbConfig.withDefaults(config)` discovers all registered adapters automatically
- Per-type classes required due to JSON-B type erasure — generic adapters can't be matched by the runtime

**Shared utility** (`evo-core`):
- `EvoTypes.isSingleStringEvoRecord(Class<?>)` — shared detection predicate used by both `evo-jackson` and `evo-jsonb`

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
