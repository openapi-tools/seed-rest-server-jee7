package io.openapitools.rest.bank.account.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.openapitools.rest.common.persistence.jpa.AbstractAuditable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Very basic modelling of account concept to show the basic use of JPA for persistence handling.
 */
@Entity
@Table(name = "BANK_ACCOUNT", uniqueConstraints = @UniqueConstraint(columnNames = { "REG_NO", "ACCOUNT_NO" }))
public class Account extends AbstractAuditable {
    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be
     * exposed out side the service since it is a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    @Column(name = "REG_NO", length = 4, nullable = false)
    private String regNo;

    @Column(name = "ACCOUNT_NO", length = 12, nullable = false)
    private String accountNo;

    @Column(name = "NAME", length = 40, nullable = false)
    private String name;


    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Transaction> transactions;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReconciledTransaction> reconciledTransactions;

    protected Account() {
        // Required by JPA
    }

    public Account(String regNo, String accountNo, String name) {
        this.regNo = regNo;
        this.accountNo = accountNo;
        this.name = name;
        transactions = new HashSet<>();
        reconciledTransactions = new HashSet<>();
        tId = UUID.randomUUID().toString();
    }

    public String getRegNo() {
        return regNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Transaction> getTransactions() {
        return Collections.unmodifiableSet(transactions);
    }

    public Set<ReconciledTransaction> getReconciledTransactions() {
        return Collections.unmodifiableSet(reconciledTransactions);
    }

    public void addTransaction(String description, BigDecimal amount) {
        transactions.add(new Transaction(this, amount, description));
    }

    public void addReconciledTransaction(Transaction transaction, Boolean reconciled, String note) {
        reconciledTransactions.add(new ReconciledTransaction(reconciled, note, transaction));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("regNo", regNo)
            .append("accountNo", accountNo)
            .append("name", name)
            .toString();
    }

}
