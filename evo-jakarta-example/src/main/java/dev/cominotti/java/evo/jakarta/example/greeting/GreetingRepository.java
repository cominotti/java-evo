package dev.cominotti.java.evo.jakarta.example.greeting;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Simple JPA repository for {@link Greeting} — no Spring Data, just plain JPA.
 *
 * <p>Manages its own {@link EntityManager} per operation with manual transactions.
 * This is the standard pattern for resource-local JPA without a container.</p>
 */
public class GreetingRepository {

    private final EntityManagerFactory emf;

    public GreetingRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Greeting save(Greeting greeting) {
        var em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(greeting);
            em.getTransaction().commit();
            return greeting;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Greeting> findAll() {
        var em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT g FROM Greeting g", Greeting.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
