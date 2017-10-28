package io.openapitools.sample.rest.bank.account.exposure.rs;


import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openapitools.sample.rest.bank.account.exposure.rs.model.TransactionRepresentation;
import io.openapitools.sample.rest.bank.account.exposure.rs.model.TransactionUpdateRepresentation;
import io.openapitools.sample.rest.bank.account.exposure.rs.model.TransactionsRepresentation;
import io.openapitools.sample.rest.bank.account.model.Account;
import io.openapitools.sample.rest.bank.account.model.Event;
import io.openapitools.sample.rest.bank.account.model.Transaction;
import io.openapitools.sample.rest.bank.account.persistence.AccountArchivist;
import io.openapitools.sample.rest.common.core.logging.LogDuration;
import io.openapitools.sample.rest.common.rs.EntityResponseBuilder;
import io.openapitools.sample.rest.common.rs.error.ErrorRepresentation;
import io.openapitools.sample.time.CurrentTime;
import io.openapitools.api.capabilities.Element;
import io.openapitools.api.capabilities.Interval;
import io.openapitools.api.capabilities.Sort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;

/**
 * REST exposure of account transactions.
 */
@Stateless
@PermitAll
@DeclareRoles("tx-system")
@Path("/accounts/{regNo}-{accountNo}/transactions")
@Api(value = "/accounts/{regNo}-{accountNo}/transactions",
    tags = {"immutable", "transactions"})
public class TransactionServiceExposure {

    private final Map<String, TransactionsProducerMethod> transactionsProducers = new HashMap<>();
    private final Map<String, TransactionProducerMethod> transactionProducers = new HashMap<>();

    @EJB
    private AccountArchivist archivist;

    public TransactionServiceExposure() {
        transactionsProducers.put("application/hal+json", this::listTransactionsSG1V1);
        transactionsProducers.put("application/hal+json;concept=transactionoverview;v=1", this::listTransactionsSG1V1);

        transactionProducers.put("application/hal+json", this::getSG1V1);
        transactionProducers.put("application/hal+json;concept=transaction;v=1", this::getSG1V1);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=transactionoverview;v=1"})
    @ApiOperation(
        value = "obtain all transactions on account for a given account", response = TransactionsRepresentation.class,
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
        tags = {"sort", "elements", "interval", "transactions"},
        produces = "application/hal+json, application/hal+json;concept=transactionoverview;v=1",
        nickname = "listTransactions"
    )
    public Response list(@Context UriInfo uriInfo, @Context Request request,
                         @HeaderParam("Accept") String accept,
                         @HeaderParam("X-Log-Token") String xLogToken,
                         @PathParam("regNo") String regNo,
                         @PathParam("accountNo") String accountNo,
                         @QueryParam("sort") String sort, @QueryParam("elements") String elements,
                         @QueryParam("interval") String interval) {
        return transactionsProducers.getOrDefault(accept, this::handleUnsupportedContentType)
            .getResponse(uriInfo, request, xLogToken, regNo, accountNo, sort, elements, interval);
    }

    @GET
    @Path("{id}")
    @Produces({"application/hal+json", "application/hal+json;concept=transaction;v=1"})
    @LogDuration(limit = 50)
    @ApiOperation(
        value = "obtain the individual single transaction from an account", response = TransactionRepresentation.class,
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
        produces = "application/hal+json, application/hal+json;concept=transaction;v=1",
        nickname = "getTransaction")
    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @HeaderParam("Accept") String accept,
                        @HeaderParam("X-Log-Token") String xLogToken,
                        @PathParam("regNo") String regNo,
                        @PathParam("accountNo") String accountNo,
                        @PathParam("id") String id) {
        return transactionProducers.getOrDefault(accept, this::handleUnsupportedContentType)
            .getResponse(uriInfo, request, xLogToken, regNo, accountNo, id);
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("tx-system")
    @Produces({"application/hal+json", "application/hal+json;concept=transaction;v=1"})
    @Consumes("application/json")
    @LogDuration(limit = 50)
    @ApiOperation(value = "creates a single transaction on an account", response = TransactionRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "system", value = "customer allows getting from coOwned account")}
        )},
        consumes = "application/json, application/json;concept=transactionupdate;v=1",
        produces = "application/hal+json, application/hal+json;concept=transaction;v=1",
        nickname = "setTransaction")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Could not create the new transaction, error describes problem",
            response = ErrorRepresentation.class),
        @ApiResponse(code = 201, message = "New transaction created at a place specified by the Location header.",
            response = TransactionRepresentation.class)})
    public Response set(@Context UriInfo uriInfo, @Context Request request,
                        @HeaderParam("X-Log-Token") String xLogToken,
                        @PathParam("regNo") String regNo, @PathParam("accountNo") String accountNo,
                        @PathParam("id") String id,
                        @ApiParam(value = "transaction") @Valid TransactionUpdateRepresentation tx) {

        Optional<Account> acc = archivist.findAccount(regNo, accountNo);
        Account a;
        Transaction t;
        if (acc.isPresent()) {
            a = acc.get();
            try {
                t = new Transaction(a, new BigDecimal(tx.getAmount()), tx.getDescription());
                a.addTransaction(t.getDescription(), t.getAmount());
                archivist.save(a);

                CacheControl cc = new CacheControl();
                int maxAge = 30;
                cc.setMaxAge(maxAge);

                TransactionRepresentation transaction = new TransactionRepresentation(t, uriInfo);
                String logToken = (xLogToken != null && !"".equals(xLogToken.trim())) ? xLogToken : UUID.randomUUID().toString();
                Response response = Response.created(URI.create(uriInfo.getPath()))
                    .entity(transaction)
                    .cacheControl(cc).expires(Date.from(CurrentTime.now().plusSeconds(maxAge)))
                    .status(201)
                    .type("application/hal+json;concept=transaction;v=1")
                    .header("X-Log-Token", logToken)
                    .header("X-RateLimit-Limit", "-1")
                    .header("X-RateLimit-Limit-24h", "-1")
                    .header("X-RateLimit-Remaining", "-1")
                    .header("X-RateLimit-Reset", "-1")
                    .build();
                Event newTX = new Event(new URI(uriInfo.getPath()), Event.getCategory(accountNo, regNo),
                    "new transaction on account " + regNo + "-" + accountNo);
                archivist.save(newTX);
                return response;
            } catch (URISyntaxException e) {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            } catch (NumberFormatException nfe) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
            }
        }
        throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
    }

    @LogDuration(limit = 50)
    public Response listTransactionsSG1V1(UriInfo uriInfo, Request request, String xLogToken,
                                          String regNo, String accountNo, String sort, String elements, String interval) {
        List<Sort> sortAs = Sort.getSortings(sort);
        Optional<Element> elementSet = Element.getElement(elements);
        Optional<Interval> withIn = Interval.getInterval(interval);
        List<Transaction> transactions = archivist.getTransactions(regNo, accountNo, elementSet, withIn, sortAs);
        return new EntityResponseBuilder<>(transactions,
            txs -> new TransactionsRepresentation(regNo, accountNo, transactions, uriInfo), xLogToken)
            .name("transactionoverview")
            .version("1")
            .maxAge(10)
            .build(request);
    }

    @LogDuration(limit = 50)
    public Response getSG1V1(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo, String id) {
        Transaction transaction = archivist.getTransaction(regNo, accountNo, id);
        return new EntityResponseBuilder<>(transaction, t -> new TransactionRepresentation(t, uriInfo), xLogToken)
            .maxAge(7 * 24 * 60 * 60)
            .name("transaction")
            .version("1")
            .build(request);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... params) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

    interface TransactionsProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken,
                             String regNo, String accountNo, String sort, String elements, String interval);
    }

    interface TransactionProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo, String id);
    }

}
