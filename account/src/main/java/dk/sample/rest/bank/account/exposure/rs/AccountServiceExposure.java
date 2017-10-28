package dk.sample.rest.bank.account.exposure.rs;


import java.net.URI;
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
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.sample.rest.bank.account.exposure.rs.model.AccountRepresentation;
import dk.sample.rest.bank.account.exposure.rs.model.AccountSparseRepresentation;
import dk.sample.rest.bank.account.exposure.rs.model.AccountUpdateRepresentation;
import dk.sample.rest.bank.account.exposure.rs.model.AccountsRepresentation;
import dk.sample.rest.bank.account.model.Account;
import dk.sample.rest.bank.account.persistence.AccountArchivist;
import dk.sample.rest.common.core.logging.LogDuration;
import dk.sample.rest.common.rs.EntityResponseBuilder;
import dk.sample.rest.common.rs.error.ErrorRepresentation;
import dk.sample.time.CurrentTime;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposing account as REST service
 */
@Stateless
@Path("/accounts")
@PermitAll
@DeclareRoles("advisor")
@Api(value = "/accounts", tags = {"accounts"})
public class AccountServiceExposure {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceExposure.class);

    private final Map<String, AccountsProducerMethod> accountsProducers = new HashMap<>();
    private final Map<String, AccountProducerMethod> accountProducers = new HashMap<>();

    @EJB
    private AccountArchivist archivist;


    public AccountServiceExposure() {
        accountsProducers.put("application/hal+json", this::listServiceGeneration1Version1);
        accountsProducers.put("application/hal+json;concept=accountoverview;v=1", this::listServiceGeneration1Version1);

        accountProducers.put("application/hal+json", this::getServiceGeneration1Version2);
        accountProducers.put("application/hal+json;concept=account;v=1", this::getServiceGeneration1Version1);
        accountProducers.put("application/hal+json;concept=account;v=2", this::getServiceGeneration1Version2);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=accountoverview;v=1"})
    @ApiOperation(value = "lists accounts", response = AccountsRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "advisor", value = "advisors are allowed getting every account"),
            @ExtensionProperty(name = "customer", value = "customer only allowed getting own accounts")}
        )},
        produces = "application/hal+json, application/hal+json;concept=accountoverview;v=1",
        notes = "List all accounts in a default projection, which is AccountOverview version 1" +
            "Supported projections and versions are: " +
            "AccountOverview in version 1 " +
            "The Accept header for the default version is application/hal+json;concept=AccountOverview;v=1.0.0.... " +
            "The format for the default version is {....}", nickname = "listAccounts")
    public Response list(@Context UriInfo uriInfo, @Context Request request,
                         @HeaderParam("Accept") String accept,
                         @HeaderParam("X-Log-Token") String xLogToken) {
        return accountsProducers.getOrDefault(accept, this::handleUnsupportedContentType).getResponse(uriInfo, request, xLogToken);
    }

    @GET
    @Path("{regNo}-{accountNo}")
    @Produces({"application/hal+json", "application/hal+json;concept=account;v=1", "application/hal+json;concept=account;v=2"})
    @ApiOperation(value = "gets the information from a single account", response = AccountRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "customer", value = "customer allows getting own account"),
            @ExtensionProperty(name = "advisor", value = "advisor allows getting every account")}
        )},
        produces = "application/hal+json, application/hal+json;concept=account;v=1, application/hal+json;concept=account;v=2",
        notes = "obtain a single account back in a default projection, which is Account version 2" +
            " Supported projections and versions are:" +
            " AccountSparse in version1 and Account in version 2" +
            " The format of the default version is .... - The Accept Header is not marked as required in the " +
            "swagger - but it is needed - we are working on a solution to that", nickname = "getAccount")
    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @PathParam("regNo") @Pattern(regexp = "^[0-9]{4}$") String regNo,
                        @PathParam("accountNo") @Pattern(regexp = "^[0-9]+$") String accountNo,
                        @HeaderParam("Accept") String accept,
                        @HeaderParam("X-Log-Token") String xLogToken) {
        LOGGER.info("Default version of account collected");
        return accountProducers.getOrDefault(accept,
            this::handleUnsupportedContentType).getResponse(uriInfo, request, xLogToken, regNo, accountNo);
    }

    @PUT
    @RolesAllowed("advisor")
    @Path("{regNo}-{accountNo}")
    @Produces({"application/hal+json"})
    @Consumes("application/json")
    @LogDuration(limit = 50)
    @ApiOperation(value = "Create new or update existing account", response = AccountRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "customer", value = "customer allows getting own account"),
            @ExtensionProperty(name = "system", value = "system allows getting coOwned account"),
            @ExtensionProperty(name = "advisor", value = "advisor allows getting every account")
        })},
        notes = "PUT is used to create a new account from scratch and may be used to alter the name of the account",
        consumes = "application/json, application/json;concept=accountupdate;v=1,",
        produces = "application/hal+json, application/hal+json;concept=account;v=1, application/hal+json;concept=account;v=2",
        nickname = "updateAccount")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Could not update or create the account", response = ErrorRepresentation.class),
        @ApiResponse(code = 201, message = "New Account Created", response = AccountRepresentation.class)})
    public Response createOrUpdate(@Context UriInfo uriInfo, @Context Request request,
                                   @PathParam("regNo") @Pattern(regexp = "^[0-9]{4}$") String regNo,
                                   @PathParam("accountNo") @Pattern(regexp = "^[0-9]+$") String accountNo,
                                   @HeaderParam("X-Log-Token") String xLogToken,
                                   @ApiParam(value = "account") @Valid AccountUpdateRepresentation account) {
        if (!regNo.equals(account.getRegNo()) || !accountNo.equals(account.getAccountNo())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Optional<Account> acc = archivist.findAccount(regNo, accountNo);
        Account a;
        if (acc.isPresent()) {
            a = acc.get();
            a.setName(account.getName());
        } else {
            a = new Account(regNo, accountNo, account.getName());
        }
        archivist.save(a);

        CacheControl cc = new CacheControl();
        int maxAge = 30;
        cc.setMaxAge(maxAge);
        String logToken = (xLogToken != null && !"".equals(xLogToken.trim())) ? xLogToken : UUID.randomUUID().toString();
        return Response.created(URI.create(uriInfo.getPath()))
            .entity(new AccountRepresentation(a, uriInfo))
            .cacheControl(cc).expires(Date.from(CurrentTime.now().plusSeconds(maxAge)))
            .status(201)
            .type("application/hal+json;concept=account;v=2")
            .header("X-Log-Token", logToken)
            .header("X-RateLimit-Limit", "-1")
            .header("X-RateLimit-Limit-24h", "-1")
            .header("X-RateLimit-Remaining", "-1")
            .header("X-RateLimit-Reset", "-1")
            .build();
    }

    Response listServiceGeneration1Version1(UriInfo uriInfo, Request request, String xLogToken) {
        List<Account> accounts = archivist.listAccounts();
        return new EntityResponseBuilder<>(accounts, list -> new AccountsRepresentation(list, uriInfo), xLogToken)
            .name("accountoverview")
            .version("1")
            .maxAge(10)
            .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version1(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo) {
        Account account = archivist.getAccount(regNo, accountNo);
        LOGGER.info("Usage - application/hal+json;concept=account;v=1");
        return new EntityResponseBuilder<>(account, acc -> new AccountSparseRepresentation(acc, uriInfo), xLogToken)
            .name("account")
            .version("1")
            .maxAge(120)
            .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version2(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo) {
        Account account = archivist.getAccount(regNo, accountNo);
        LOGGER.info("Usage - application/hal+json;concept=account;v=2");
        return new EntityResponseBuilder<>(account, acc -> new AccountRepresentation(acc, acc.getTransactions(), uriInfo), xLogToken)
            .name("account")
            .version("2")
            .maxAge(60)
            .build(request);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... params) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

    interface AccountsProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken);
    }

    interface AccountProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String regNo, String accountNo);
    }

}
