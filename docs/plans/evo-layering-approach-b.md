# Approach B — Universal Persistable Wrappers

> Module structure: Alternative 4 (build-level isolation).
> Persistable wrappers: for **all** Value Objects (single-component and composite).
> Every domain VO has a corresponding `Persistable*` embeddable in the persistence module.

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
  PersistableEmail, PersistableCpf, ...    ← @Embeddable wrappers for single-component EVOs
  PersistableFullName, PersistableAddress  ← @Embeddable wrappers for composites

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
annotations are absent. Identical to Approach A.

### Composite domain VO

```java
public record FullName(FirstName firstName, LastName lastName) {}
```

Identical to Approach A.

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

`autoApply = true` is needed because the `@Embeddable` wrapper holds `Email value` (not
`String value`) and Hibernate must know how to convert `Email` to a database column type.

### Single-component embeddable wrapper

```java
@Embeddable
public record PersistableEmail(
        @Column(name = "email", length = EmailRules.MAX_LENGTH)
        Email value
) {}
```

Encodes default column name (`"email"`) and length (`320`) once. Entity fields inherit these
defaults via `@Embedded` and override them via `@AttributeOverride` when needed.

The `autoApply = true` converter for `Email` is applied automatically inside the embeddable — no
`@Convert` annotation needed on the component.

### Composite embeddable wrapper

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

Same pattern as Approach A for composites.

## Entity usage — uniform pattern for all EVOs

### Single-component EVOs — via @Embedded wrapper

```java
@Entity
public class Customer {

    // Default column: name="email", length=320
    @Embedded
    private PersistableEmail email;

    // Override column name only — length=320 inherited (see critical assumption below)
    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "billing_email"))
    private PersistableEmail billingEmail;

    // Override column name and add NOT NULL
    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "primary_email", nullable = false))
    private PersistableEmail primaryEmail;

    // Domain-facing accessors
    public Email getEmail() {
        return email == null ? null : email.value();
    }
    public void setEmail(Email email) {
        this.email = email == null ? null : new PersistableEmail(email);
    }
}
```

### Composite VOs — via @Embedded wrapper (same pattern)

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

Identical to Approach A. `EvoModule` checks `@EvoType` instead of `@Embeddable`.

## Linter impact

Zero changes. Identical to Approach A.

## Critical finding: @AttributeOverride REPLACES, does not merge

**Verified empirically on Hibernate 7.0 + H2 (Spring Boot 4.0.3, Java 25).**

`@AttributeOverride` performs a **full replacement** of the embeddable's `@Column`. When the
override specifies only `name` but omits `length`, the column gets `length = 255` (the `@Column`
annotation default), **NOT** the embeddable's original `length`.

### Evidence

The existing `Greeting` entity demonstrates this bug. DDL generated by Hibernate:

```sql
create table greeting (
    email varchar(320),           -- no override → embeddable's length=320 preserved
    author_cpf varchar(255),      -- @AttributeOverride(name only) → length RESET to 255!
    company_cnpj varchar(255),    -- @AttributeOverride(name only) → length RESET to 255!
    tax_id varchar(14),           -- @Convert + explicit @Column → correct length=14
    ...
)
```

The `authorCpf` column should be `VARCHAR(11)` (from `CpfRules.DIGIT_COUNT`) and `companyCnpj`
should be `VARCHAR(14)` (from `CnpjRules.DIGIT_COUNT`), but both are `VARCHAR(255)` because the
`@AttributeOverride` omits `length`.

Verified by `AttributeOverrideColumnInheritanceTest` querying H2's `INFORMATION_SCHEMA.COLUMNS`.

### Root cause

Java annotations are immutable value objects. At the reflection API level, there is no way to
distinguish "developer wrote `length = 255`" from "annotation default is `length = 255`".
When Hibernate reads the `@Column` from `@AttributeOverride`, it receives a fully-populated
annotation proxy with all defaults filled in. Merging with the embeddable's original `@Column`
is impossible without a spec-level mechanism that does not exist.

See also: [Jakarta Persistence issue #322](https://github.com/jakartaee/persistence/issues/322)
(filed by Steve Ebersole, Hibernate lead) — proposes `columnPrefix`/`columnPostfix` on
`@Embedded` as a lightweight alternative.

### Impact on this approach

This means every `@AttributeOverride` must **re-specify all `@Column` attributes** that differ
from the annotation defaults, including `length`:

```java
// WRONG — length resets to 255
@Embedded
@AttributeOverride(name = "value",
    column = @Column(name = "billing_email"))
private PersistableEmail billingEmail;

// CORRECT — must re-specify length
@Embedded
@AttributeOverride(name = "value",
    column = @Column(name = "billing_email", length = EmailRules.MAX_LENGTH))
private PersistableEmail billingEmail;
```

This weakens the reuse benefit: developers must still remember to specify `length` in every
`@AttributeOverride`, using the `*Rules` constant. The embeddable's `@Column` defaults are only
effective when NO `@AttributeOverride` is applied at all (i.e., the default column name is
acceptable).

### Revised entity usage pattern

```java
@Entity
public class Customer {

    // Default column: name="email", length=320 — works perfectly, no override needed
    @Embedded
    private PersistableEmail email;

    // Override column name — MUST re-specify length
    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "billing_email", length = EmailRules.MAX_LENGTH))
    private PersistableEmail billingEmail;

    // Override column name and add NOT NULL — MUST re-specify length
    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "primary_email", length = EmailRules.MAX_LENGTH, nullable = false))
    private PersistableEmail primaryEmail;
}
```

## Tradeoffs

### Strengths

- Default column metadata works when no `@AttributeOverride` is needed (first usage with default
  column name) — no risk of forgotten `length` in that case.
- Uniform pattern for all EVOs (single-component and composite) — one rule for developers.
- Type-safe: `PersistableEmail` constructor requires an `Email`, not a raw `String`.
- `@AttributeOverride` is the single override mechanism — explicit "I'm changing the default".

### Weaknesses

- **`@AttributeOverride` resets unspecified `@Column` attributes to annotation defaults.**
  Every override must re-specify `length` (and any other non-default attribute), weakening the
  reuse benefit. The `*Rules` constants help, but must be remembered.
- Every single-component EVO gets a `Persistable*` wrapper — more types in the persistence module.
- Entity accessors must wrap/unwrap for every EVO field (~2 lines per getter/setter).
- HQL paths go through the embeddable: `WHERE g.email.value = :email`.
- Spring Data derived queries: `findByEmailValue(Email e)` or `findByEmail_Value(Email e)`.
