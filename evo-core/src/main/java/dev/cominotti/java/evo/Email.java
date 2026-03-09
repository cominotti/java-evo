package dev.cominotti.java.evo;

import dev.cominotti.java.evo.validation.EmailRules;
import dev.cominotti.java.evo.validation.EvoValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@EvoType
public record Email(
        @NotBlank(message = EmailRules.BLANK_MESSAGE)
        @jakarta.validation.constraints.Email(message = EmailRules.FORMAT_MESSAGE)
        @Size(max = EmailRules.MAX_LENGTH, message = EmailRules.MAX_LENGTH_MESSAGE)
        String value
) {

    public Email {
        EvoValidation.validate(Email.class, "value", value);
    }

    @Override
    public String toString() {
        return value;
    }
}
