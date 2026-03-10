# Approach A — Selective Persistable Wrappers

> Module structure: Alternative 4 (build-level isolation).
> Persistable wrappers: only for **composite** Value Objects (multi-column).
> Single-component EVOs: use `autoApply = true` converters directly on entity fields.

## Module layout

```
java-evo-core/               ← jakarta.validation only (NO jakarta.persistence)
  @EvoType
  Email, Cpf, Cnpj, FirstName, LastName   ← single-String records
  CpfOrCnpj                               ← sealed interface
  FullName, Address                        ← composite domain records (not EVOs)
  EvoValidation, *Rules, *Constraints      ← validation infrastructure

java-evo-persistence/        ← jakarta.persistence (depends on java-evo-core)
  StringEvoConverter<T>                    ← abstract base for single-component converters
  EmailConverter, CpfConverter, ...        ← @Converter(autoApply = true)
  CpfOrCnpjConverter                       ← @Converter(autoApply = true) — safe now
  PersistableFullName, PersistableAddress  ← @Embeddable wrappers for composites only

java-evo-jackson/            ← jackson (depends on java-evo-core)
  EvoModule                                ← flat-string ser/des for @EvoType records
```

## Domain layer (java-evo-core)

### Single-component EVO

```java
@EvoType
public record Email(
        @NotBlank(message = EmailRules.BLANK_MESSAGE)
        @Email(message = EmailRules.FORMAT_MESSAGE)
        @Size(max = EmailRules.MAX_LENGTH, message = EmailRules.MAX_LENGTH_MESSAGE)
        String value
) {
    public Email { EvoValidation.validate(Email.class, "value", value); }
    @Override public String toString() { return value; }
}
```

No `@Embeddable`, no `@Column`. Jakarta Validation annotations are present; Jakarta Persistence
annotations are absent.

### Composite domain VO

```java
public record FullName(FirstName firstName, LastName lastName) {}
```

Not annotated with `@EvoType` (it is not a single-String value type). Composes two
EVOs into a domain concept.

## Persistence layer (java-evo-persistence)

### Converter base class

```java
public abstract class StringEvoConverter<T> implements AttributeConverter<T, String> {
    private final Function<T, String> toDb;
    private final Function<String, T> fromDb;

    protected StringEvoConverter(Function<T, String> toDb, Function<String, T> fromDb) {
        this.toDb = toDb;
        this.fromDb = fromDb;
    }

    @Override public String convertToDatabaseColumn(T evo) {
        return evo == null ? null : toDb.apply(evo);
    }
    @Override public T convertToEntityAttribute(String value) {
        return value == null ? null : fromDb.apply(value);
    }
}
```

### Single-component converter

```java
@Converter(autoApply = true)
public class EmailConverter extends StringEvoConverter<Email> {
    public EmailConverter() { super(Email::value, Email::new); }
}
```

One-liner. `autoApply = true` is safe because `Email` is not `@Embeddable` (the Hibernate 7
conflict that previously forced `autoApply = false` on `CpfOrCnpjConverter` no longer applies).

### Composite embeddable (only for multi-column groupings)

```java
@Embeddable
public record PersistableFullName(
        @Column(name = "first_name", length = 100)
        FirstName firstName,

        @Column(name = "last_name", length = 100)
        LastName lastName
) {
    public static PersistableFullName of(FullName fn) {
        return new PersistableFullName(fn.firstName(), fn.lastName());
    }
    public FullName toDomain() {
        return new FullName(firstName, lastName);
    }
}
```

`autoApply` converters for `FirstName` and `LastName` are applied automatically inside the
embeddable — no `@Convert` annotation needed on the components.

## Entity usage

### Single-component EVOs — direct on entity field

```java
@Entity
public class Customer {

    @Column(name = "email", length = EmailRules.MAX_LENGTH, nullable = false)
    private Email email;

    @Column(name = "billing_email", length = EmailRules.MAX_LENGTH)
    private Email billingEmail;

    // Accessors — zero wrapping overhead
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
}
```

Column metadata (`name`, `length`, `nullable`) is specified on each entity field. Constants like
`EmailRules.MAX_LENGTH` provide a shared source of truth for length, but developers must remember
to use them.

### Composite VOs — via @Embedded wrapper

```java
@Entity
public class Person {

    @Embedded
    private PersistableFullName name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName",
            column = @Column(name = "guardian_first_name")),
        @AttributeOverride(name = "lastName",
            column = @Column(name = "guardian_last_name"))
    })
    private PersistableFullName guardianName;

    public FullName getName() {
        return name == null ? null : name.toDomain();
    }
    public void setName(FullName fn) {
        this.name = fn == null ? null : PersistableFullName.of(fn);
    }
}
```

## Jackson layer (java-evo-jackson)

`EvoModule` changes its detection predicate from `@Embeddable` to `@EvoType`:

```java
static boolean isSingleStringEvoRecord(Class<?> type) {
    if (!type.isRecord()) return false;
    if (!type.isAnnotationPresent(EvoType.class)) return false;
    RecordComponent[] components = type.getRecordComponents();
    return components.length == 1 && components[0].getType() == String.class;
}
```

## Linter impact

Zero changes. The linter matches `@EvoType` by name. EVOs without `@Embeddable` are
still excluded from primitive-type findings.

## Tradeoffs

### Strengths

- Single-component EVOs have zero wrapper overhead — entity accessors are direct.
- Fewer types in the persistence module (only composites get wrappers).
- Cleaner HQL: `WHERE g.email = :email` (no `.value` path through embeddable).
- Spring Data derived queries: `findByEmail(Email e)` — natural.

### Weaknesses (addressed by Approach A Enhanced below)

- Column metadata for single-component EVOs is scattered across entity fields.
- Developers must remember to use constants like `EmailRules.MAX_LENGTH` on every field.
- No "default column name" concept for single-component EVOs — always specified per field.
- Mixed patterns: `@Column` for single EVOs, `@Embedded` for composites — two rules.
- Code review must verify `@Column` attributes on every single-component EVO field.

---

## Approach A Enhanced — Automatic Column Length via Integrator

> **Superseded:** The original `@EvoColumn` + `@AttributeBinderType` approach was replaced by
> `EvoColumnMetadataIntegrator` (Hibernate `Integrator`). Entity fields now use standard
> `@Column` — column length is auto-derived from `@Size(max)` on the EVO type's `value` field
> when the column has the JPA default length (255). Explicit `@Column(length = N)` overrides.
>
> **Verified on Hibernate 7.2.4 + Spring Boot 4.0.3 + Java 25.** The Integrator +
> `autoApply` converter interaction works correctly: 212 tests pass, column metadata is verified
> via H2 `INFORMATION_SCHEMA.COLUMNS` queries.

### How it works

A single `@EvoColumn` annotation + `EvoColumnBinder` pair in `evo/persistence/` handles all
EVO types:

```java
// Annotation — unified for all EVO types
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@AttributeBinderType(binder = EvoColumnBinder.class)
public @interface EvoColumn {
    String name();
    int length() default -1;       // -1 = derive from @Size(max) on the EVO type
    boolean nullable() default false;
}

// Binder — derives length from @Size(max) on the EVO type's value field via reflection,
// or uses the explicit length attribute when provided
public class EvoColumnBinder implements AttributeBinder<EvoColumn> {
    @Override
    public void bind(EvoColumn annotation, MetadataBuildingContext ctx,
                     PersistentClass entity, Property property) {
        var column = (Column) property.getColumns().get(0);
        column.setName(annotation.name());
        column.setLength(annotation.length() > 0
                ? annotation.length()
                : deriveLengthFromEvoType(property));   // reads @Size(max) via reflection
        column.setNullable(annotation.nullable());
    }

    /** Reads @Size(max) from the EVO type's {@code value} field. */
    private int deriveLengthFromEvoType(Property property) { /* ... */ }
}
```

For EVO types that carry `@Size(max=N)` on their `value` field (e.g., `Email`), length is
derived automatically — no `length` attribute needed. For types without `@Size` (e.g., `Cpf`,
`Cnpj`), length is specified explicitly via `@EvoColumn(length = CpfRules.DIGIT_COUNT)`.

The `autoApply = true` converter (e.g., `EmailConverter`) handles `Email ↔ String` conversion.
The binder sets column metadata. Both compose on the same entity field.

### Entity usage

```java
@Entity
public class Greeting {

    @EvoColumn(name = "email")                        // length derived from @Size(max=320) on Email
    private Email email;

    @EvoColumn(name = "author_cpf", length = CpfRules.DIGIT_COUNT)  // explicit length=11
    private Cpf authorCpf;

    @EvoColumn(name = "company_cnpj", length = CnpjRules.DIGIT_COUNT)  // explicit length=14
    private Cnpj companyCnpj;

    @EvoColumn(name = "tax_id", length = CnpjRules.DIGIT_COUNT)     // explicit length=14
    @Convert(converter = CpfOrCnpjConverter.class)    // explicit — autoApply conflicts
    private CpfOrCnpj taxId;

    // Direct accessors — no wrapping needed
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
}
```

### CpfOrCnpj autoApply caveat

`CpfOrCnpjConverter` uses `autoApply = false` (explicit `@Convert` required) because `Cpf` and
`Cnpj` implement `CpfOrCnpj`, and their own `autoApply = true` converters conflict with a
supertype converter. Hibernate 7 throws "Multiple auto-apply converters matched" when both a
supertype and subtype converter have `autoApply = true`. Sealed interface fields also need an
explicit `length` on `@EvoColumn` since `deriveLengthFromEvoType()` reads `@Size` from concrete
types only.

### Strengths over vanilla Approach A

- Column length is either **derived from `@Size(max)`** or **explicitly specified** — impossible to forget or misstate.
- Single annotation for all EVO types — `@EvoColumn` replaces per-type `@EmailColumn`, `@CpfColumn`, etc.
- No new annotation + binder pair needed when adding a new EVO type — just use `@EvoColumn`.
- One annotation per field — `@EvoColumn(name = "email")` is more expressive than
  `@Column(name = "email", length = 320)`.
- Same domain type everywhere — controllers, domain, entities all use `Email`.
- Direct entity accessors — no `Persistable*` wrapper unwrapping.

### Tradeoffs

- **Hibernate-specific** — `@AttributeBinderType` is a Hibernate ORM API (`@Incubating`), not
  JPA standard. Not portable to EclipseLink. However, Hibernate uses it internally for
  `@TenantId`, suggesting it is stable in practice.
- **Sealed interface types need explicit `@Convert` and explicit `length`** — due to the
  autoApply conflict with subtype converters, and because `deriveLengthFromEvoType()` cannot
  read `@Size` from sealed interfaces.
- **EVO types without `@Size`** require explicit `length` on `@EvoColumn` — but the compiler
  enforces this via the required `name` attribute, and missing length for non-`@Size` types
  will fail fast at boot time.
