package dev.cominotti.java.evo.greeting;

import java.time.LocalDateTime;

import dev.cominotti.java.evo.Cnpj;
import dev.cominotti.java.evo.Cpf;
import dev.cominotti.java.evo.CpfOrCnpj;
import dev.cominotti.java.evo.Email;
import dev.cominotti.java.evo.persistence.CpfOrCnpjConverter;
import dev.cominotti.java.evo.persistence.EvoColumn;
import dev.cominotti.java.evo.validation.CnpjRules;
import dev.cominotti.java.evo.validation.CpfRules;
import jakarta.persistence.Convert;
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

    @EvoColumn(name = "author_cpf", length = CpfRules.DIGIT_COUNT, nullable = true)
    private Cpf authorCpf;

    @EvoColumn(name = "company_cnpj", length = CnpjRules.DIGIT_COUNT, nullable = true)
    private Cnpj companyCnpj;

    @EvoColumn(name = "tax_id", length = CnpjRules.DIGIT_COUNT, nullable = true)
    @Convert(converter = CpfOrCnpjConverter.class)
    private CpfOrCnpj taxId;

    protected Greeting() {
    }

    public Greeting(String name, String message) {
        this.name = name;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Cpf getAuthorCpf() {
        return authorCpf;
    }

    public void setAuthorCpf(Cpf authorCpf) {
        this.authorCpf = authorCpf;
    }

    public Cnpj getCompanyCnpj() {
        return companyCnpj;
    }

    public void setCompanyCnpj(Cnpj companyCnpj) {
        this.companyCnpj = companyCnpj;
    }

    public CpfOrCnpj getTaxId() {
        return taxId;
    }

    public void setTaxId(CpfOrCnpj taxId) {
        this.taxId = taxId;
    }
}
