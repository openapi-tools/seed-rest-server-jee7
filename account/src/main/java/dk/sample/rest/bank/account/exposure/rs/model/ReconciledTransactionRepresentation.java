package dk.sample.rest.bank.account.exposure.rs.model;

import javax.ws.rs.core.UriInfo;

import dk.sample.rest.bank.account.exposure.rs.ReconciledTransactionServiceExposure;
import dk.sample.rest.bank.account.exposure.rs.TransactionServiceExposure;
import dk.sample.rest.bank.account.model.ReconciledTransaction;
import dk.sample.rest.bank.account.model.Transaction;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * Represents a single transaction as returned by the REST service in a default projection.
 */
@Resource
@ApiModel(value = "Reconciledtransaction",
        description = "A mutable decorator for a transaction to keep its immutability")
public class ReconciledTransactionRepresentation {

    private String id;

    private String note;

    private Boolean reconciled;

    @Link
    private HALLink self;

    @Link
    private HALLink transaction;

    public ReconciledTransactionRepresentation(ReconciledTransaction reconciledTransaction, Transaction transaction, UriInfo uriInfo) {
        this.id = reconciledTransaction.getId();
        this.note = reconciledTransaction.getNote();
        this.reconciled = reconciledTransaction.getReconciled();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(ReconciledTransactionServiceExposure.class)
                .path(ReconciledTransactionServiceExposure.class, "get")
                .build(transaction.getAccount().getRegNo(), transaction.getAccount().getAccountNo(), reconciledTransaction.getId()))
                .build();
        this.transaction = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(TransactionServiceExposure.class)
                .path(TransactionServiceExposure.class, "get")
                .build(transaction.getAccount().getRegNo(), transaction.getAccount().getAccountNo(), transaction.getId()))
                .build();

    }

    @ApiModelProperty(
            access = "public",
            name = "id",
            value = "a semantic (here shown as UUID) identifier for the transaction.")

    public String getId() {
        return id;
    }

    @ApiModelProperty(
            access = "public",
            name = "note",
            value = "contains information relevant to the reconciled decorated transaction.")
    public String getNote() {
        return note;
    }

    @ApiModelProperty(
            access = "public",
            name = "reconciled",
            value = "signals whether the transaction is reconciled or not.")
    public Boolean getReconciled() {
        return reconciled;
    }

    @ApiModelProperty(
            access = "public",
            name = "transaction",
            notes = "the decorated transaction.")
    public HALLink getTransaction() {
        return transaction;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the reconciledtransaction itself.")
    public HALLink getSelf() {
        return self;
    }
}
