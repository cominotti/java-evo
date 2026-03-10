package dev.cominotti.java.evo.jakarta.example.greeting;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Jakarta REST resource for greetings — mirrors the Spring MVC
 * {@code GreetingController} in {@code evo-spring-example}.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingResource {

    private final GreetingRepository repository;

    @Inject
    public GreetingResource(GreetingRepository repository) {
        this.repository = repository;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello, World!";
    }

    @GET
    @Path("/hello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloName(@PathParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @POST
    @Path("/greetings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Greeting createGreeting(@Valid GreetingRequest request) {
        var greeting = new Greeting(request.name(), request.message());
        greeting.setEmail(request.email());
        return repository.save(greeting);
    }

    @GET
    @Path("/greetings")
    public List<Greeting> listGreetings() {
        return repository.findAll();
    }
}
