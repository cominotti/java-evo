// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.greeting;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private final GreetingRepository repository;

    public GreetingController(GreetingRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping(value = "/hello/{name}", produces = "text/plain")
    public String helloName(@PathVariable String name) {
        return "Hello, " + name + "!";
    }

    @PostMapping("/greetings")
    public Greeting createGreeting(@Valid @RequestBody GreetingRequest request) {
        var greeting = new Greeting(request.name(), request.message());
        greeting.setEmail(request.email());
        return repository.save(greeting);
    }

    @GetMapping("/greetings")
    public List<Greeting> listGreetings() {
        return repository.findAll();
    }
}
