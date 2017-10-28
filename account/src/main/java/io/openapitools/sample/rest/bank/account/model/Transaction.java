package io.openapitools.sample.rest.bank.account.model;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.openapitools.sample.rest.common.persistence.jpa.AbstractAuditable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Very basic modelling of an transaction concept to show the relation to account handled by JPA.
 */
@Entity
@Table(name = "BANK_TRANSACTION", uniqueConstraints = @UniqueConstraint(columnNames = { "FK_ACCOUNT_TID", "SID" }))
public class Transaction extends AbstractAuditable {
    private static final String[] EXCLUDED_FIELDS = new String[]{
        "tId", "account", "lastModifiedBy", "lastModifiedTime"
    };

    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be
     * exposed out side the service since it is a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    /**
     * Semantic key of a transaction which is exposed as key to the outside world!
     */
    @Column(name = "SID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    /**
     * The transaction is "owned" by account.
     */
    @ManyToOne
    @JoinColumn(name = "FK_ACCOUNT_TID", nullable = false)
    private Account account;

    @Column(name = "AMOUNT", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal amount;

    @Column(name = "DESCRIPTION", length = 500, nullable = false)
    private String description;

    protected Transaction() {
        // Required by JPA
    }

    public Transaction(Account account, BigDecimal amount, String description) {
        this.account = account;
        this.amount = amount;
        this.description = description;
        tId = UUID.randomUUID().toString();
        // The semantic key might as well be generated as a hash value of the transaction values
        // for simplicity it is just a unique id here.
        id = UUID.randomUUID().toString();
    }

    /**
     * @param sid a controlled human readable and url capable identifier
     */
    public Transaction(String sid, Account account, BigDecimal amount, String description) {
        this(account, amount, description);
        this.id = sid;
    }

    public String getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    protected String[] excludedFields() {
        return EXCLUDED_FIELDS;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("account", account)
            .append("id", id)
            .append("amount", amount)
            .append("description", description)
            .toString();
    }

}
