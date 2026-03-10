// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jakarta.example.greeting;

import dev.cominotti.java.evo.jakarta.example.JakartaEvoApplication;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GreetingResource} using Jersey's built-in
 * test framework — no Spring, no MockMvc.
 *
 * <p>Mirrors the test coverage from {@code GreetingControllerIntegrationTest}
 * in {@code evo-spring-example}: valid/invalid/null EVO fields, error format,
 * and round-trip persistence.</p>
 */
class GreetingResourceIntegrationTest extends JerseyTest {

    @Override
    protected Application configure() {
        return JakartaEvoApplication.createResourceConfig();
    }

    /**
     * JerseyTest lifecycle methods bridged to JUnit 5.
     * JerseyTest was designed for JUnit 4; these overrides make it work with JUnit 5.
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void helloReturnsGreeting() {
        var response = target("/hello").request().get(String.class);
        assertThat(response).isEqualTo("Hello, World!");
    }

    @Test
    void helloWithNameReturnsPersonalizedGreeting() {
        var response = target("/hello/Jakarta").request().get(String.class);
        assertThat(response).isEqualTo("Hello, Jakarta!");
    }

    @Test
    void postValidGreetingReturnsPersistedEntity() {
        var response = target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Alice", "message": "Hello from Jakarta REST!"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
        var body = response.readEntity(String.class);
        assertThat(body).contains("\"name\":\"Alice\"")
                .contains("\"id\":");
    }

    @Test
    void postGreetingWithValidEmailReturnsEmailInResponse() {
        var response = target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Carol", "message": "Hi!", "email": "carol@example.com"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
        var body = response.readEntity(String.class);
        assertThat(body).contains("\"email\":\"carol@example.com\"");
    }

    @Test
    void postGreetingWithoutEmailIsAccepted() {
        var response = target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Dave", "message": "Hi!"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void postGreetingWithNullEmailIsAccepted() {
        var response = target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Frank", "message": "Hi!", "email": null}
                        """, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void postGreetingWithInvalidEmailReturnsBadRequest() {
        var response = target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Eve", "message": "Hi!", "email": "not-an-email"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(400);
        var body = response.readEntity(String.class);
        assertThat(body).contains("\"title\":\"Validation failed\"")
                .contains("\"errors\"");
    }

    @Test
    void postGreetingWithEmptyEmailReturnsBadRequest() {
        var response = target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Grace", "message": "Hi!", "email": ""}
                        """, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void getGreetingsReturnsList() {
        // Create one first
        target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "Bob", "message": "Round-trip test"}
                        """, MediaType.APPLICATION_JSON));

        var response = target("/greetings").request().get(String.class);
        assertThat(response).startsWith("[");
    }

    @Test
    void fullRoundTripWithEvoField() {
        target("/greetings").request()
                .post(Entity.entity("""
                        {"name": "EvoTrip", "message": "Round-trip", "email": "evo@example.com"}
                        """, MediaType.APPLICATION_JSON));

        var response = target("/greetings").request().get(String.class);
        assertThat(response).contains("\"email\":\"evo@example.com\"");
    }
}
