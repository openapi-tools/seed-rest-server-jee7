package io.openapitools.sample.rest.bank.account.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import io.openapitools.sample.rest.bank.account.exposure.rs.ReconciledTransactionServiceExposure;
import io.openapitools.sample.rest.bank.account.model.Account;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of reconciled transactions as returned by the REST service.
 */
@Resource
@ApiModel(value = "ReconciledTransactions",
        description = "A list containing the mutable decorator instances each having link to their immutable " +
                "counterpart transaction to keep its immutability")
public class ReconciledTransactionsRepresentation {
    @EmbeddedResource("reconciledTransactions")
    private Collection<ReconciledTransactionRepresentation> rtxs;

    @Link
    private HALLink self;

    public ReconciledTransactionsRepresentation(Account account, UriInfo uriInfo) {
        rtxs = new ArrayList<>();
        rtxs.addAll(account.getReconciledTransactions().stream()
                .map(rtx -> new ReconciledTransactionRepresentation(rtx, rtx.getTransaction(), uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(ReconciledTransactionServiceExposure.class)
                .build(account.getRegNo(), account.getAccountNo()))
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "reconciledtransactions",
            value = "the list of reconciled transaction.")
    public Collection<ReconciledTransactionRepresentation> getReconciledTransactions() {
        return Collections.unmodifiableCollection(rtxs);
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the reconciled transaction list.")
    public HALLink getSelf() {
        return self;
    }
}
