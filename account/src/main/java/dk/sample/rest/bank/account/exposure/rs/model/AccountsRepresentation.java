package dk.sample.rest.bank.account.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import dk.sample.rest.bank.account.exposure.rs.AccountServiceExposure;
import dk.sample.rest.bank.account.model.Account;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of accounts from the REST service exposure in this default projection.
 */
@Resource
@ApiModel(value = "Accounts",
        description = "a list of Accounts in default projection")
public class AccountsRepresentation {

    @Link
    private HALLink self;

    @EmbeddedResource("accounts")
    private Collection<AccountRepresentation> accounts;

    public AccountsRepresentation(List<Account> accounts, UriInfo uriInfo) {
        this.accounts = new ArrayList<>();
        this.accounts.addAll(accounts.stream().map(account -> new AccountRepresentation(account, uriInfo)).collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(AccountServiceExposure.class)
            .build())
            .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the account list itself.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "accounts",
            value = "account list.")
    public Collection<AccountRepresentation> getAccounts() {
        return Collections.unmodifiableCollection(accounts);
    }
}
