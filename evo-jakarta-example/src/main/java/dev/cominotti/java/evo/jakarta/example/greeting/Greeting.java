package dev.cominotti.java.evo.jakarta.example.greeting;

import java.time.LocalDateTime;

import dev.cominotti.java.evo.Email;
import dev.cominotti.java.evo.persistence.EvoColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Greeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String message;

    private LocalDateTime createdAt;

    @EvoColumn(name = "email", nullable = true)
    private Email email;

    protected Greeting() {
    }

    public Greeting(String name, String message) {
        this.name = name;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
}
