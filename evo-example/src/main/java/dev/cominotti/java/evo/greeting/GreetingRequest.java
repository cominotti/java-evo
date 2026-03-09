package dev.cominotti.java.evo.greeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GreetingRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 500) String message
) {
}
