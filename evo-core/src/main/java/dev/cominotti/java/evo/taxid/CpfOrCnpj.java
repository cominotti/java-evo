// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.taxid;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoMessages;

@EvoType
public sealed interface CpfOrCnpj permits Cpf, Cnpj {

    String value();

    static CpfOrCnpj parse(String formatted) {
        if (formatted == null || formatted.isBlank()) {
            throw new IllegalArgumentException(EvoMessages.resolve(TaxIdRules.TAX_ID_BLANK_MESSAGE));
        }
        return CpfOrCnpj.of(formatted.replaceAll("\\D", ""));
    }

    static CpfOrCnpj of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(EvoMessages.resolve(TaxIdRules.TAX_ID_BLANK_MESSAGE));
        }
        return switch (value.length()) {
            case 11 -> new Cpf(value);
            case 14 -> new Cnpj(value);
            default -> throw new IllegalArgumentException(
                    EvoMessages.resolve(TaxIdRules.TAX_ID_LENGTH_MESSAGE, value.length()));
        };
    }
}
