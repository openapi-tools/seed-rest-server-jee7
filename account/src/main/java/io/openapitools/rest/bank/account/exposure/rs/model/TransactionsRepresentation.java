package io.openapitools.rest.bank.account.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import io.openapitools.rest.bank.account.exposure.rs.TransactionServiceExposure;
import io.openapitools.rest.bank.account.model.Account;
import io.openapitools.rest.bank.account.model.Transaction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of transactions as returned by the REST service in the default projection.
 */
@Resource
@ApiModel(value = "Transactions",
        description = "A set of immutable transactions")

public class TransactionsRepresentation {
    @EmbeddedResource("transactions")
    private Collection<TransactionRepresentation> transactions;

    @Link
    private HALLink self;

    public TransactionsRepresentation(Account account, UriInfo uriInfo) {
        transactions = new ArrayList<>();
        transactions.addAll(account.getTransactions().stream()
                .map(transaction -> new TransactionRepresentation(transaction, uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(TransactionServiceExposure.class)
                .build(account.getRegNo(), account.getAccountNo()))
                .build();
    }

    public TransactionsRepresentation(String regNo, String accountNo, List<Transaction> txs, UriInfo uriInfo) {
        transactions = new ArrayList<>();
        transactions.addAll(txs.stream()
                .map(tx -> new TransactionRepresentation(tx, uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(TransactionServiceExposure.class)
                .build(regNo, accountNo))
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "transactions",
            value = "the list of transaction.")
    public Collection<TransactionRepresentation> getTransactions() {
        return Collections.unmodifiableCollection(transactions);
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the transactionlist itself.")
    public HALLink getSelf() {
        return self;
    }
}
