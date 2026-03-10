// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.greeting;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GreetingRepositoryTest {

    @Autowired
    private GreetingRepository repository;

    @Test
    void saveAndRetrieveById() {
        var greeting = new Greeting("Alice", "Hello from JPA!");
        Greeting saved = repository.save(greeting);

        Optional<Greeting> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getMessage()).isEqualTo("Hello from JPA!");
    }

    @Test
    void savedEntityHasGeneratedId() {
        var greeting = new Greeting("Bob", "Testing ID generation");
        Greeting saved = repository.save(greeting);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void savedEntityRetainsCreatedAt() {
        var greeting = new Greeting("Charlie", "Timestamp test");
        Greeting saved = repository.save(greeting);

        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findAllReturnsMultipleGreetings() {
        repository.save(new Greeting("Alice", "First"));
        repository.save(new Greeting("Bob", "Second"));
        repository.save(new Greeting("Charlie", "Third"));

        List<Greeting> all = repository.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    void emptyRepositoryReturnsEmptyList() {
        List<Greeting> all = repository.findAll();

        assertThat(all).isEmpty();
    }
}
