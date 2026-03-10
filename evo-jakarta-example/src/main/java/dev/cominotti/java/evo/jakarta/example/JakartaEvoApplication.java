// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jakarta.example;

import java.net.URI;
import java.util.logging.Logger;

import dev.cominotti.java.evo.jakarta.example.greeting.GreetingRepository;
import dev.cominotti.java.evo.jakarta.example.greeting.GreetingResource;
import dev.cominotti.java.evo.rest.EvoConstraintViolationExceptionMapper;
import dev.cominotti.java.evo.rest.EvoJsonbExceptionMapper;
import dev.cominotti.java.evo.rest.EvoParamConverterProvider;
import jakarta.persistence.Persistence;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Standalone Jakarta REST application using Jersey + Grizzly.
 * No Spring — pure Jakarta EE stack.
 *
 * <p>Demonstrates the full EVO integration: JSON-B flat-string serialization
 * via {@code evo-jsonb}, error handling via {@code evo-rest}, and JPA
 * persistence via {@code evo-persistence}.</p>
 */
public class JakartaEvoApplication {

    private static final Logger LOG = Logger.getLogger(JakartaEvoApplication.class.getName());

    public static final String BASE_URI = "http://localhost:8081/";

    @SuppressWarnings("java:S1172") // args is required by the JVM entry point contract
    public static void main(String[] args) throws Exception {
        var server = startServer();
        LOG.info("Jakarta EVO application started at " + BASE_URI);
        LOG.info("Press Enter to stop...");
        System.in.read();
        server.shutdown();
    }

    /**
     * Creates and starts the Grizzly HTTP server with the Jersey application.
     * Also used by integration tests.
     */
    public static HttpServer startServer() {
        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), createResourceConfig());
    }

    /**
     * Builds the Jersey {@link ResourceConfig} with all resources, providers,
     * and the JPA repository bound via HK2.
     */
    public static ResourceConfig createResourceConfig() {
        var emf = Persistence.createEntityManagerFactory("evo-jakarta");
        var repository = new GreetingRepository(emf);

        var config = new ResourceConfig();

        // Resources
        config.register(GreetingResource.class);

        // EVO providers
        config.register(EvoJsonbExceptionMapper.class);
        config.register(EvoConstraintViolationExceptionMapper.class);
        config.register(EvoParamConverterProvider.class);

        // JSON-B with EVO flat-string adapters via ContextResolver<Jsonb>.
        // Jersey's JsonBindingFeature picks this up to create its Jsonb instance.
        // Must use a concrete class (not a lambda) so Jersey can resolve the
        // generic type parameter Jsonb from ContextResolver<Jsonb>.
        config.register(new EvoJsonbContextResolver());

        // HK2 dependency injection — bind the repository
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(repository).to(GreetingRepository.class);
            }
        });

        return config;
    }
}
