package dev.cominotti.java.evo;

import java.util.Optional;

import dev.cominotti.java.evo.greeting.Greeting;
import dev.cominotti.java.evo.greeting.GreetingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EvoPersistenceIntegrationTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    @Autowired
    private GreetingRepository repository;

    @Test
    void saveAndReloadWithEmail() {
        var greeting = new Greeting("Alice", "Hello!");
        greeting.setEmail(new Email("alice@example.com"));

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isNotNull();
        assertThat(found.get().getEmail().value()).isEqualTo("alice@example.com");
    }

    @Test
    void saveAndReloadWithCpf() {
        var greeting = new Greeting("Bob", "Hi!");
        greeting.setAuthorCpf(new Cpf(VALID_CPF));

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAuthorCpf()).isNotNull();
        assertThat(found.get().getAuthorCpf().value()).isEqualTo(VALID_CPF);
    }

    @Test
    void saveAndReloadWithCnpj() {
        var greeting = new Greeting("Charlie", "Hey!");
        greeting.setCompanyCnpj(new Cnpj(VALID_CNPJ));

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCompanyCnpj()).isNotNull();
        assertThat(found.get().getCompanyCnpj().value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void saveAndReloadWithCpfOrCnpjAsCpf() {
        var greeting = new Greeting("Dan", "Greetings!");
        greeting.setTaxId(new Cpf(VALID_CPF));

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTaxId()).isInstanceOf(Cpf.class);
        assertThat(found.get().getTaxId().value()).isEqualTo(VALID_CPF);
    }

    @Test
    void saveAndReloadWithCpfOrCnpjAsCnpj() {
        var greeting = new Greeting("Eve", "Hi there!");
        greeting.setTaxId(new Cnpj(VALID_CNPJ));

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTaxId()).isInstanceOf(Cnpj.class);
        assertThat(found.get().getTaxId().value()).isEqualTo(VALID_CNPJ);
    }

    @Test
    void saveAndReloadWithNullEvoFields() {
        var greeting = new Greeting("Frank", "No EVOs");

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isNull();
        assertThat(found.get().getAuthorCpf()).isNull();
        assertThat(found.get().getCompanyCnpj()).isNull();
        assertThat(found.get().getTaxId()).isNull();
    }

    @Test
    void saveAndReloadWithAllEvoFieldsPopulated() {
        var greeting = new Greeting("Grace", "Full EVOs!");
        greeting.setEmail(new Email("grace@example.com"));
        greeting.setAuthorCpf(new Cpf(VALID_CPF));
        greeting.setCompanyCnpj(new Cnpj(VALID_CNPJ));
        greeting.setTaxId(new Cpf(VALID_CPF));

        Greeting saved = repository.save(greeting);
        repository.flush();

        Optional<Greeting> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        var g = found.get();
        assertThat(g.getEmail().value()).isEqualTo("grace@example.com");
        assertThat(g.getAuthorCpf().value()).isEqualTo(VALID_CPF);
        assertThat(g.getCompanyCnpj().value()).isEqualTo(VALID_CNPJ);
        assertThat(g.getTaxId()).isInstanceOf(Cpf.class);
        assertThat(g.getTaxId().value()).isEqualTo(VALID_CPF);
    }
}
