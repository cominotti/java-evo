package dev.cominotti.java.evo.taxid;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoMessages;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@EvoType
public record Cnpj(
        @NotBlank(message = CnpjRules.BLANK_MESSAGE)
        @Pattern(regexp = CnpjRules.REGEX, message = CnpjRules.FORMAT_MESSAGE)
        @NotAllSameDigit(message = CnpjRules.ALL_SAME_DIGIT_MESSAGE)
        @CnpjCheckDigit(message = CnpjRules.CHECK_DIGIT_MESSAGE)
        String value
) implements CpfOrCnpj {

    public Cnpj {
        EvoValidation.validate(Cnpj.class, "value", value);
    }

    public static Cnpj parse(String formatted) {
        if (formatted == null) {
            throw new IllegalArgumentException(EvoMessages.resolve(CnpjRules.NULL_MESSAGE));
        }
        return new Cnpj(formatted.replaceAll("\\D", ""));
    }

    @Override
    public String toString() {
        return value;
    }
}
