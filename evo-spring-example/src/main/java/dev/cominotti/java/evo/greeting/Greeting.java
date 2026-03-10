// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.greeting;

import java.time.LocalDateTime;

import dev.cominotti.java.evo.email.Email;
import dev.cominotti.java.evo.persistence.CpfOrCnpjConverter;
import dev.cominotti.java.evo.taxid.Cnpj;
import dev.cominotti.java.evo.taxid.CnpjRules;
import dev.cominotti.java.evo.taxid.Cpf;
import dev.cominotti.java.evo.taxid.CpfOrCnpj;
import dev.cominotti.java.evo.taxid.CpfRules;
import jakarta.persistence.Column;
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

    @Column(name = "email")
    private Email email;

    @Column(name = "author_cpf", length = CpfRules.DIGIT_COUNT)
    private Cpf authorCpf;

    @Column(name = "company_cnpj", length = CnpjRules.DIGIT_COUNT)
    private Cnpj companyCnpj;

    @Column(name = "tax_id", length = CnpjRules.DIGIT_COUNT)
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
