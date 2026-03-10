package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * JSON-B adapter for the {@link CpfOrCnpj} sealed interface.
 *
 * <p>Serializes via {@link CpfOrCnpj#value()} (works for both {@code Cpf} and
 * {@code Cnpj}). Deserializes via {@link CpfOrCnpj#of(String)} which dispatches
 * by digit count (11 → {@code Cpf}, 14 → {@code Cnpj}).</p>
 *
 * <p>This adapter does not extend {@link StringEvoJsonbAdapter} because
 * {@code CpfOrCnpj} is a sealed interface — not a single-component record.
 * It has no canonical {@code (String)} constructor; dispatch logic lives in
 * {@code CpfOrCnpj.of()}.</p>
 */
public class CpfOrCnpjJsonbAdapter implements JsonbAdapter<CpfOrCnpj, String>, EvoJsonbAdapterProvider {

    @Override
    public String adaptToJson(CpfOrCnpj obj) {
        return obj.value();
    }

    @Override
    public CpfOrCnpj adaptFromJson(String value) {
        try {
            return CpfOrCnpj.of(value);
        } catch (IllegalArgumentException e) {
            throw new JsonbException(e.getMessage(), e);
        }
    }
}
