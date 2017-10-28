package dk.sample.rest.bank.customer.exposure.rs;

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

import dk.sample.rest.bank.customer.exposure.rs.model.CustomerRepresentation;
import dk.sample.rest.bank.customer.exposure.rs.model.CustomerUpdateRepresentation;
import dk.sample.rest.bank.customer.exposure.rs.model.CustomersRepresentation;
import dk.sample.rest.bank.customer.model.Customer;
import dk.sample.rest.bank.customer.persistence.CustomerArchivist;
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
 * Exposing customer as REST service
 */
@Stateless
@Path("/customers")
@PermitAll
@DeclareRoles("advisor")
@Api(value = "/customers", tags = {"customers"})
public class CustomerServiceExposure {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceExposure.class);

    private final Map<String, CustomersProducerMethod> customersProducers = new HashMap<>();
    private final Map<String, CustomerProducerMethod> customerProducers = new HashMap<>();

    @EJB
    private CustomerArchivist archivist;


    public CustomerServiceExposure() {
        customersProducers.put("application/hal+json", this::listServiceGeneration1Version1);
        customersProducers.put("application/hal+json;concept=customers;v=1", this::listServiceGeneration1Version1);

        customerProducers.put("application/hal+json", this::getServiceGeneration1Version2);
        customerProducers.put("application/hal+json;concept=customer;v=1", this::getServiceGeneration1Version1);
        customerProducers.put("application/hal+json;concept=customer;v=2", this::getServiceGeneration1Version2);
    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=customers;v=1"})
    @ApiOperation(value = "lists customers", response = CustomersRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "advisor", value = "advisors are allowed getting every customer"),
            @ExtensionProperty(name = "customer", value = "customer only allowed getting own information")}
        )},
        produces = "application/hal+json, application/hal+json;concept=customers;v=1",
        notes = "List all customers in a default projection, which is Customers version 1" +
            "Supported projections and versions are: " +
            "Customers in version 1 " +
            "The Accept header for the default version is application/hal+json;concept=customers;v=1.0.0.... " +
            "The format for the default version is {....}", nickname = "listCustomers")
    public Response list(@Context UriInfo uriInfo, @Context Request request,
                         @HeaderParam("Accept") String accept,
                         @HeaderParam("X-Log-Token") String xLogToken) {
        return customersProducers.getOrDefault(accept, this::handleUnsupportedContentType).getResponse(uriInfo, request, xLogToken);
    }

    @GET
    @Path("{customerNo}")
    @Produces({"application/hal+json", "application/hal+json;concept=customer;v=1", "application/hal+json;concept=customer;v=2"})
    @ApiOperation(value = "gets the information from a single customer", response = CustomerRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "customer", value = "customer allows getting own information"),
            @ExtensionProperty(name = "advisor", value = "advisor allows getting all information")}
        )},
        produces = "application/hal+json, application/hal+json;concept=customer;v=1, application/hal+json;concept=customer;v=2",
        notes = "obtain a single customer back in a default projection, which is Customer version 2" +
            " Supported projections and versions are:" +
            " Customer in version1 and Customer in version 2" +
            " The format of the default version is .... - The Accept Header is not marked as required in the " +
            "swagger - but it is needed - we are working on a solution to that", nickname = "getCustomer")
    public Response get(@Context UriInfo uriInfo, @Context Request request,
                        @PathParam("customerNo") @Pattern(regexp = "^[0-9]{10}$") String customerNo,
                        @HeaderParam("Accept") String accept,
                        @HeaderParam("X-Log-Token") String xLogToken) {
        LOGGER.info("Default version of customer collected");
        return customerProducers.getOrDefault(accept, this::handleUnsupportedContentType).getResponse(uriInfo, request, xLogToken, customerNo);
    }

    @PUT
    @RolesAllowed("advisor")
    @Path("{customerNo}")
    @Produces({"application/hal+json"})
    @Consumes("application/json")
    @LogDuration(limit = 50)
    @ApiOperation(value = "Create new or update existing customer", response = CustomerRepresentation.class,
        authorizations = {
            @Authorization(value = "oauth2", scopes = {}),
            @Authorization(value = "oauth2-cc", scopes = {}),
            @Authorization(value = "oauth2-ac", scopes = {}),
            @Authorization(value = "oauth2-rop", scopes = {}),
            @Authorization(value = "Bearer")
        },
        extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "customer", value = "customer allows getting own customer"),
            @ExtensionProperty(name = "system", value = "system allows getting coOwned customer"),
            @ExtensionProperty(name = "advisor", value = "advisor allows getting every customer")
        })},
        notes = "PUT is used to create a new customer from scratch and may be used to alter the name of the customer",
        consumes = "application/json, application/json;concept=customerupdate;v=1",
        produces = "application/hal+json, application/hal+json;concept=customer;v=1, application/hal+json;concept=customer;v=2",
        nickname = "updateCustomer")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Could unfortunately not update or create the new customer",
            response = ErrorRepresentation.class),
        @ApiResponse(code = 201, message = "New Customer Created", response = CustomerRepresentation.class)})
    public Response createOrUpdate(@Context UriInfo uriInfo, @Context Request request,
                                   @PathParam("customerNo") @Pattern(regexp = "^[0-9]+$") String customerNo,
                                   @HeaderParam("X-Log-Token") String xLogToken,
                                   @ApiParam(value = "customer") @Valid CustomerUpdateRepresentation customer) {
        if (!customerNo.equals(customer.getNumber())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Optional<Customer> cust = archivist.findCustomer(customerNo);
        Customer c;
        if (cust.isPresent()) {
            c = cust.get();
            c.setSirname(customer.getFirstName());
            c.setSirname(customer.getMiddleName());
            c.setSirname(customer.getSirname());
        } else {
            c = new Customer(customer.getFirstName(), customer.getMiddleName(), customer.getSirname());
        }
        archivist.save(c);

        CacheControl cc = new CacheControl();
        int maxAge = 30;
        cc.setMaxAge(maxAge);
        String logToken = (xLogToken != null && !"".equals(xLogToken.trim())) ? xLogToken : UUID.randomUUID().toString();
        return Response.created(URI.create(uriInfo.getPath()))
            .entity(new CustomerRepresentation(c, uriInfo))
            .cacheControl(cc).expires(Date.from(CurrentTime.now().plusSeconds(maxAge)))
            .status(201)
            .type("application/hal+json;concept=customer;v=2")
            .header("X-Log-Token", logToken)
            .header("X-RateLimit-Limit", "-1")
            .header("X-RateLimit-Limit-24h", "-1")
            .header("X-RateLimit-Remaining", "-1")
            .header("X-RateLimit-Reset", "-1")
            .build();
    }

    Response listServiceGeneration1Version1(UriInfo uriInfo, Request request, String xLogToken) {
        List<Customer> customers = archivist.listCustomers();
        return new EntityResponseBuilder<>(customers, list -> new CustomersRepresentation(list, uriInfo), xLogToken)
            .name("customers")
            .version("1")
            .maxAge(10)
            .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version1(UriInfo uriInfo, Request request, String xLogToken, String customerNo) {
        Customer customer = archivist.getCustomer(customerNo);
        LOGGER.info("Usage - application/hal+json;concept=customer;v=1");
        return new EntityResponseBuilder<>(customer, cust -> new CustomerRepresentation(cust, uriInfo), xLogToken)
            .name("customer")
            .version("1")
            .maxAge(120)
            .build(request);
    }

    @LogDuration(limit = 50)
    Response getServiceGeneration1Version2(UriInfo uriInfo, Request request, String xLogToken, String customerNo) {
        Customer customer = archivist.getCustomer(customerNo);
        LOGGER.info("Usage - application/hal+json;concept=customer;v=2 - customer = " + customer);
        return new EntityResponseBuilder<>(customer, cust -> new CustomerRepresentation(cust, uriInfo), xLogToken)
            .name("customer")
            .version("2")
            .maxAge(60)
            .build(request);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... params) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }

    interface CustomersProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken);
    }

    interface CustomerProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String customerNo);
    }

}
