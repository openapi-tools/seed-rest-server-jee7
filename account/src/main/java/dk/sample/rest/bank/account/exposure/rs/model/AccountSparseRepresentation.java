package dk.sample.rest.bank.account.exposure.rs.model;

import java.util.Set;

import javax.ws.rs.core.UriInfo;

import dk.sample.rest.bank.account.exposure.rs.AccountServiceExposure;
import dk.sample.rest.bank.account.exposure.rs.TransactionServiceExposure;
import dk.sample.rest.bank.account.model.Account;
import dk.sample.rest.bank.account.model.Transaction;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * Represents a single Account as returned from REST service in the sparse projection.
 */
@Resource
@ApiModel(value = "AccountSparse",
        description = "the Account kin a sparse projection")

public class AccountSparseRepresentation {
    private String regNo;
    private String accountNo;
    private String name;

    @Link("account:transactions")
    private HALLink transactionsResource;

    @Link
    private HALLink self;

    public AccountSparseRepresentation(Account account, Set<Transaction> transactions, UriInfo uriInfo) {
        this(account, uriInfo);
    }

    public AccountSparseRepresentation(Account account, UriInfo uriInfo) {
        this.regNo = account.getRegNo();
        this.accountNo = account.getAccountNo();
        this.name = account.getName();
        this.transactionsResource = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(TransactionServiceExposure.class)
            .build(account.getRegNo(), account.getAccountNo())).build();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(AccountServiceExposure.class)
            .path(AccountServiceExposure.class, "get")
            .build(account.getRegNo(), account.getAccountNo()))
            .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "regno",
            example = "1234",
            value = "the registration number preceeding the account  number.")
    public String getRegNo() {
        return regNo;
    }

    @ApiModelProperty(
            access = "public",
            name = "accountno",
            example = "12345678",
            value = "the account  number.")
    public String getAccountNo() {
        return accountNo;
    }

    @ApiModelProperty(
            access = "public",
            name = "name",
            example = "NemKonto",
            value = "the human readable name of the account.")
    public String getName() {
        return name;
    }

    @ApiModelProperty(
            access = "public",
            name = "transactions",
            value = "link to the collection of transactions that have taken place for the account.")
    public HALLink getTransactionsResource() {
        return transactionsResource;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the account itself.")
    public HALLink getSelf() {
        return self;
    }
}
