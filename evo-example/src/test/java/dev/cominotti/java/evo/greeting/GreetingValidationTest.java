package dev.cominotti.java.evo.greeting;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GreetingValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void validRequestHasNoViolations() {
        var request = new GreetingRequest("Alice", "Hello!");
        Set<ConstraintViolation<GreetingRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankNameIsRejected() {
        var request = new GreetingRequest("", "Hello!");
        Set<ConstraintViolation<GreetingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void nameExceedingMaxLengthIsRejected() {
        var request = new GreetingRequest("A".repeat(101), "Hello!");
        Set<ConstraintViolation<GreetingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void blankMessageIsRejected() {
        var request = new GreetingRequest("Alice", "");
        Set<ConstraintViolation<GreetingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("message"));
    }

    @Test
    void messageExceedingMaxLengthIsRejected() {
        var request = new GreetingRequest("Alice", "X".repeat(501));
        Set<ConstraintViolation<GreetingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("message"));
    }

    @Test
    void multipleInvalidFieldsProduceMultipleViolations() {
        var request = new GreetingRequest("", "");
        Set<ConstraintViolation<GreetingRequest>> violations = validator.validate(request);
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }
}
