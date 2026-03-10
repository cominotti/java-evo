package dev.cominotti.java.evo.taxid;

import dev.cominotti.java.evo.EvoType;
import dev.cominotti.java.evo.validation.EvoMessages;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@EvoType
public record Cpf(
        @NotBlank(message = CpfRules.BLANK_MESSAGE)
        @Pattern(regexp = CpfRules.REGEX, message = CpfRules.FORMAT_MESSAGE)
        @NotAllSameDigit(message = CpfRules.ALL_SAME_DIGIT_MESSAGE)
        @CpfCheckDigit(message = CpfRules.CHECK_DIGIT_MESSAGE)
        String value
) implements CpfOrCnpj {

    public Cpf {
        EvoValidation.validate(Cpf.class, "value", value);
    }

    public static Cpf parse(String formatted) {
        if (formatted == null) {
            throw new IllegalArgumentException(EvoMessages.resolve(CpfRules.NULL_MESSAGE));
        }
        return new Cpf(formatted.replaceAll("\\D", ""));
    }

    @Override
    public String toString() {
        return value;
    }
}
