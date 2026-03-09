package dev.cominotti.java.evo.persistence;

import dev.cominotti.java.evo.Cpf;
import jakarta.persistence.Converter;

/**
 * Auto-applied JPA converter for {@link Cpf} ↔ {@code String}.
 */
@Converter(autoApply = true)
public class CpfConverter extends StringEvoConverter<Cpf> {

    public CpfConverter() {
        super(Cpf::value, Cpf::new);
    }
}
