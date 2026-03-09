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
    void postInvalidGreetingReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "message": ""}
                                """))
                .andExpect(status().isBadRequest());
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
}
