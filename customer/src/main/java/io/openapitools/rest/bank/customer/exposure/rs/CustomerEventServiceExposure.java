package io.openapitools.rest.bank.customer.exposure.rs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.nykredit.api.capabilities.Interval;
import io.openapitools.rest.bank.customer.exposure.rs.model.EventRepresentation;
import io.openapitools.rest.bank.customer.exposure.rs.model.EventsRepresentation;
import io.openapitools.rest.bank.customer.model.Event;
import io.openapitools.rest.bank.customer.persistence.CustomerArchivist;
import io.openapitools.rest.common.core.logging.LogDuration;
import io.openapitools.rest.common.rs.EntityResponseBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * REST exposure of events that are related to the customer.
 */
@Stateless
@PermitAll
@Path("/customer-events")
@DeclareRoles("tx-system")
@Api(value = "/customer-events",
     tags = {"events"})
public class CustomerEventServiceExposure {
    private final Map<String, EventsProducerMethod> eventsProducers = new HashMap<>();
    private final Map<String, EventProducerMethod> eventProducers = new HashMap<>();
    private final Map<String, EventsCategoryProducerMethod> eventCategoryProducers = new HashMap<>();

    @EJB
    private CustomerArchivist archivist;

    public CustomerEventServiceExposure() {
        eventsProducers.put("application/hal+json", this::listAllSG1V1);
        eventsProducers.put("application/hal+json;concept=events;v=1", this::listAllSG1V1);

        eventProducers.put("application/hal+json", this::getSG1V1);
        eventProducers.put("application/hal+json;concept=event;v=1", this::getSG1V1);

        eventCategoryProducers.put("application/hal+json", this::listByCategorySG1V1);
        eventCategoryProducers.put("application/hal+json;concept=eventcategory;v=1", this::listByCategorySG1V1);

    }

    @GET
    @Produces({"application/hal+json", "application/hal+json;concept=events;v=1"})
    @ApiOperation(
            value = "obtain all events emitted by the customer-event service", response = EventsRepresentation.class,
            notes = " the events are signalled by this resource as this this is the authoritative resource for all events that " +
                    "subscribers to the customers service should be able to listen for and react to. In other words this is the " +
                    "authoritative feed for the customers service",
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            tags = {"interval", "events"},
            produces = "application/hal+json,  application/hal+json;concept=events;v=1",
            nickname = "listAllCustomerEvents"
        )
    public Response listAll(@Context UriInfo uriInfo, @Context Request request,
                            @HeaderParam("Accept") String accept,
                            @HeaderParam("X-Log-Token") String xLogToken,
                            @QueryParam("interval") String interval) {
        return eventsProducers.getOrDefault(accept, this::handleUnsupportedContentType)
                .getResponse(uriInfo, request, xLogToken, interval);
    }


    @GET
    @Path("{category}")
    @Produces({ "application/hal+json", "application/hal+json;concept=eventcategory;v=1"})
    @ApiOperation(value = "obtain all events scoped to a certain category", response = EventsRepresentation.class,
            notes = " the events are signalled by this resource as this this is the authoritative resource for all events that " +
                    "subscribers to the customer service should be able to listen for and react to. In other words this is the " +
                    "authoritative feed for the customer service, allowing for subscribers to have these grouped into categories",
            authorizations = {
                    @Authorization(value = "oauth2", scopes = {}),
                    @Authorization(value = "oauth2-cc", scopes = {}),
                    @Authorization(value = "oauth2-ac", scopes = {}),
                    @Authorization(value = "oauth2-rop", scopes = {}),
                    @Authorization(value = "Bearer")
            },
            tags = {"interval", "events"},
            produces = "application/hal+json,  application/hal+json;concept=eventcategory;v=1",
            nickname = "getCustomerEventsByCategory"
        )
    public Response getByCategory(@Context UriInfo uriInfo, @Context Request request,
                                  @HeaderParam("Accept") String accept,
                                  @HeaderParam("X-Log-Token") String xLogToken,
                                  @PathParam("category") @Pattern(regexp = "^[a-zA-Z0-9-]{36}]") String category,
                                  @QueryParam("interval") String interval) {
        return eventCategoryProducers.getOrDefault(accept, this::handleUnsupportedContentType)
                .getResponse(uriInfo, request, xLogToken, category, interval);
    }

    @GET
    @Path("{category}/{id}")
    @Produces({ "application/hal+json", "application/hal+json;concept=event;v=1" })
    @LogDuration(limit = 50)
    @ApiOperation(
            value = "obtain the individual events from an customer", response = EventRepresentation.class,
            notes = "the event her is immutable and thus can be cached for a long time",
            authorizations = {
                @Authorization(value = "oauth2", scopes = {}),
                @Authorization(value = "oauth2-cc", scopes = {}),
                @Authorization(value = "oauth2-ac", scopes = {}),
                @Authorization(value = "oauth2-rop", scopes = {}),
                @Authorization(value = "Bearer")
            },
            tags = {"immutable", "events"},
            produces = "application/hal+json,  application/hal+json;concept=event;v=1",
            nickname = "getCustomerEvent")
    public Response getSingle(@Context UriInfo uriInfo, @Context Request request,
                              @HeaderParam("Accept") String accept,
                              @HeaderParam("X-Log-Token") String xLogToken,
                              @PathParam("category") @Pattern(regexp = "^[a-zA-Z0-9-]{36}]") String category,
                              @PathParam("id") String id) {
        return eventProducers.getOrDefault(accept, this::handleUnsupportedContentType)
                .getResponse(uriInfo, request, xLogToken, category, id);
    }

    @LogDuration(limit = 50)
    public Response listAllSG1V1(UriInfo uriInfo, Request request, String xLogToken, String interval) {
        Optional<Interval> withIn = Interval.getInterval(interval);
        List<Event> events = archivist.findEvents(withIn);
        return new EntityResponseBuilder<>(events, txs -> new EventsRepresentation(events, uriInfo), xLogToken)
                .name("events")
                .version("1")
                .maxAge(60)
                .build(request);
    }

    @LogDuration(limit = 50)
    public Response listByCategorySG1V1(UriInfo uriInfo, Request request, String xLogToken, String category, String interval) {
        Optional<Interval> withIn = Interval.getInterval(interval);
        List<Event> events = archivist.getEventsForCategory(category, withIn);
        return new EntityResponseBuilder<>(events, txs -> new EventsRepresentation(events, uriInfo), xLogToken)
                .name("eventcategory")
                .version("1")
                .maxAge(60)
                .build(request);
    }

    @LogDuration(limit = 50)
    public Response getSG1V1(UriInfo uriInfo, Request request, String xLogToken, String category, String id) {
        Event event = archivist.getEvent(category, id);
        return new EntityResponseBuilder<>(event, e -> new EventRepresentation(e, uriInfo), xLogToken)
                .maxAge(7 * 24 * 60 * 60)
                .name("event")
                .version("1")
                .build(request);
    }

    interface EventsProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String interval);
    }

    interface EventProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String category, String id);
    }

    interface EventsCategoryProducerMethod {
        Response getResponse(UriInfo uriInfo, Request request, String xLogToken, String interval, String category);
    }

    Response handleUnsupportedContentType(UriInfo uriInfo, Request request, String... params) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }


}
