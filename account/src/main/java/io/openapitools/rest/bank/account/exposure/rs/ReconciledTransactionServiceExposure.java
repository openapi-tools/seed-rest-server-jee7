package io.openapitools.rest.bank.account.exposure.rs;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.nykredit.api.capabilities.Sanitizer;
import io.openapitools.rest.bank.account.exposure.rs.model.ReconciledTransactionRepresentation;
import io.openapitools.rest.bank.account.exposure.rs.model.ReconciledTransactionUpdateRepresentation;
import io.openapitools.rest.bank.account.exposure.rs.model.ReconciledTransactionsRepresentation;
import io.openapitools.rest.bank.account.model.Account;
import io.openapitools.rest.bank.account.model.ReconciledTransaction;
import io.openapitools.rest.bank.account.model.Transaction;
import io.openapitools.rest.bank.account.persistence.AccountArchivist;
import io.openapitools.rest.common.core.logging.LogDuration;
import io.openapitools.rest.common.rs.EntityResponseBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;

/**
 * REST service exposing the Reconciled transactions
 */
@Stateless
@Path("/accounts/{regNo}-{accountNo}/reconciled-transactions")
@Api(value = "/accounts/{regNo}-{accountNo}/reconciled-transactions",
    tags = {"decorator", "reconciled"})
public class ReconciledTransactionServiceExposure {
    private final Map<String, ReconciledTransactionsProducerMethod> reconciledTxsProducers = new HashMap<>();
    private final Map<String, ReconciledTransactionProducerMethod> reconciledTxProducers = new HashMap<>();


    @EJB
    private AccountArchivist archivist;

    public ReconciledTransactionServiceExposure() {
        reconciledTxsProducers.put("application/hal+json", this::listReconciledTransactionsSG1V1);
        reconciledTxsProducers.put("application/hal+json;concept=reconciledtransactions;v=1", this::listReconciledTransactionsSG1V1);

        reconciledTxProducers.put("application/hal+json", this::getReconciledSG1V1);
        reconciledTxProducers.put("application/hal+json;concept=reconciledtransaction;v=1", this::getReconciledSG1V1);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=reconciledtransactions;v=1"})
    @ApiOperation(value = "obtain reconciled transactions (added API capabilities not though not implemented)",
        response = ReconciledTransactionsRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "customer", value = "customer allows getting from own account"),
            @ExtensionProperty(name = "advisor", value = "advisor allows getting from every account")}
        )},
        tags = {"select", "sort", "elements", "interval", "filter", "embed", "decorator", "reconciled"},
        notes = "obtain a list of all reconciled transactions from an account" +
            "the reconciled transactions are user controlled checks and notes for transactions " +
            "such as - Yes I have verified that this transaction was correct and thus it is reconciled",
        produces = "application/hal+json, application/hal+json;concept=reconciledtransactions;v=1",
        nickname = "listReconciledTransactions")
    public Response list(@Context UriInfo uriInfo, @Context Request request,
                         @HeaderParam("Accept") String accept,
                         @HeaderParam("X-Log-Token") String xLogToken,
                         @PathParam("regNo") String regNo, @PathParam("accountNo") String accountNo) {
        return reconciledTxsProducers.getOrDefault(accept, this::handleUnsupportedContentType)
            .getResponse(uriInfo, request, xLogToken, regNo, accountNo);
    }

    @GET
    @Path("{id}")
    @Produces({"application/hal+json", "application/hal+json;concept=reconciledtransaction;v=1"})
    @LogDuration(limit = 50)
    @ApiOperation(value = "obtain a single reconciled transaction from a given account", response = ReconciledTransactionRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "customer", value = "customer allows getting own account")}
        )},
        produces = "application/hal+json, application/hal+json;concept=reconciledtransaction;v=1",
        nickname = "getReconciledTransaction")

    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @HeaderParam("Accept") String accept,
                        @HeaderParam("X-Log-Token") String xLogToken,
                        @PathParam("regNo") String regNo,
                        @PathParam("accountNo") String accountNo, @PathParam("id") String id) {
        return reconciledTxProducers.getOrDefault(accept, this::handleUnsupportedContentType)
            .getResponse(uriInfo, request, xLogToken, regNo, accountNo, id);
    }

    @PUT
    @Path("{id}")
    @Produces({"application/hal+json", "application/hal+json;concept=reconciledtransaction;v=1"})
    @Consumes(MediaType.APPLICATION_JSON)
    @LogDuration(limit = 50)
    @ApiOperation(value = "Create new or update reconciled transaction", response = ReconciledTransactionRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "system", value = "customer allows getting coOwned account")}
        )},
        notes = "reconciled transactions are user controlled checks and notes for transactions" +
            "such as - Yes I have verified that this transaction was correct and thus it is reconciled",
        consumes = "application/json, application/json;concept=reconciledtransactionupdate;v=1",
        produces = "application/hal+json, application/hal+json;concept=reconciledtransaction;v=1",
        nickname = "updateReconciledTransaction")
    public Response createOrUpdate(@Context UriInfo uriInfo, @Context Request request,
                                   @PathParam("regNo") @Pattern(regexp = "^[0-9]{4}$") String regNo,
                                   @PathParam("accountNo") @Pattern(regexp = "^[0-9]+$") String accountNo,
                                   @PathParam("id") String id,
                                   @HeaderParam("X-Log-Token") String xLogToken,
                                   @ApiParam(value = "reconciled transaction") @Valid ReconciledTransactionUpdateRepresentation rtx) {


        String txId = Sanitizer.sanitize(id, false, true);
        Transaction tx = archivist.findTransaction(regNo, accountNo, txId);
        if (!defined(tx)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        ReconciledTransaction reconciled = new ReconciledTransaction(rtx.getReconciled().contains("true"), rtx.getNote(), tx);
        archivist.save(reconciled);
        return new EntityResponseBuilder<>(reconciled.getTransaction(),
            t -> new ReconciledTransactionRepresentation(reconciled, t, uriInfo), xLogToken)
            .name("reconciledtransaction")
            .version("1")
            .maxAge(60)
            .build(request);
    }

    @LogDuration(limit = 50)
    public Response listReconciledTransactionsSG1V1(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo) {
        Account account = archivist.getAccount(regNo, accountNo);
        return new EntityResponseBuilder<>(account.getReconciledTransactions(),
            transactions -> new ReconciledTransactionsRepresentation(account, uriInfo), xLogToken)
            .name("reconciledtransactions")
            .version("1")
            .maxAge(10)
            .build(request);
    }

    @LogDuration(limit = 50)
    public Response getReconciledSG1V1(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo, String id) {
        ReconciledTransaction reconciledTransaction = archivist.getReconciledTransaction(regNo, accountNo, id);
        return new EntityResponseBuilder<>(reconciledTransaction,
            rt -> new ReconciledTransactionRepresentation(rt, rt.getTransaction(), uriInfo), xLogToken)
            .maxAge(24 * 60 * 60)
            .name("reconciledtransaction")
            .version("1")
            .build(request);
    }

    private boolean defined(Transaction tx) {
        return tx != null;
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... parms) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

    interface ReconciledTransactionsProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo);
    }

    interface ReconciledTransactionProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo, String id);
    }

}
