package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import jakarta.persistence.Converter;

/**
 * JPA converter for {@link CpfOrCnpj} ↔ {@code String}.
 *
 * <p>{@code autoApply = false} is required because {@link dev.cominotti.java.evo.taxid.Cpf}
 * and {@link dev.cominotti.java.evo.taxid.Cnpj} implement {@code CpfOrCnpj}, and their own
 * {@code autoApply = true} converters ({@link CpfConverter}, {@link CnpjConverter}) conflict
 * with this one on fields typed as the concrete subtype. Hibernate 7 throws
 * "Multiple auto-apply converters matched" when both a supertype and subtype converter
 * have {@code autoApply = true}.</p>
 *
 * <p>Entity fields of type {@code CpfOrCnpj} must use explicit
 * {@code @Column(name = "...", length = CnpjRules.DIGIT_COUNT)} since
 * {@code EvoColumnMetadataIntegrator} only derives length from {@code @Size} on
 * single-String EVO records, not from sealed interfaces.</p>
 *
 * <p>Deserialization via {@link CpfOrCnpj#of(String)} dispatches by value length:
 * 11 digits → {@code Cpf}, 14 digits → {@code Cnpj}.</p>
 */
@Converter
public class CpfOrCnpjConverter extends StringEvoConverter<CpfOrCnpj> {

    public CpfOrCnpjConverter() {
        super(CpfOrCnpj::value, CpfOrCnpj::of);
    }
}
