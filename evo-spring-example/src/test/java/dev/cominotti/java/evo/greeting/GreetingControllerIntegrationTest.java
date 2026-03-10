package dev.cominotti.java.evo.greeting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloReturnsGreeting() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void helloWithNameReturnsPersonalizedGreeting() throws Exception {
        mockMvc.perform(get("/hello/Spring"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring!"));
    }

    @Test
    void postValidGreetingReturnsPersistedEntity() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Alice", "message": "Hello from integration test!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.message").value("Hello from integration test!"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void postInvalidGreetingReturnsBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "message": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void getGreetingsReturnsList() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Bob", "message": "Round-trip test"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void postGreetingWithValidEmailReturnsEmailInResponse() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Carol", "message": "Hi!", "email": "carol@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carol@example.com"));
    }

    @Test
    void postGreetingWithoutEmailIsAccepted() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Dave", "message": "Hi!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void postGreetingWithExplicitNullEmailIsAccepted() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Frank", "message": "Hi!", "email": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void postGreetingWithInvalidEmailReturnsBadRequestWithEvoMessage() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Eve", "message": "Hi!", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void postGreetingWithEmptyEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Grace", "message": "Hi!", "email": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json at all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullRoundTrip() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Roundtrip", "message": "End-to-end test"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Roundtrip' && @.message == 'End-to-end test')]").exists());
    }

    @Test
    void invalidEvoInNestedObjectReportsFieldWithDottedPath() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "message": "B", "contact": {"workEmail": "bad"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("contact.workEmail"));
    }

    @Test
    void invalidEvoInDeeplyNestedObjectReportsFullDottedPath() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "message": "B", "contact": {"address": {"confirmationEmail": "bad"}}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("contact.address.confirmationEmail"));
    }

    @Test
    void nullNestedContactIsAccepted() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "message": "B"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void fullRoundTripWithEvoField() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "EvoTrip", "message": "Round-trip with email", "email": "evo@example.com"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'EvoTrip')].email").value("evo@example.com"));
    }
}
