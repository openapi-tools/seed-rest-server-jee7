package io.openapitools.sample.rest.bank.account.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import io.openapitools.sample.rest.bank.account.exposure.rs.AccountEventServiceExposure;
import io.openapitools.sample.rest.bank.account.model.Event;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of events as returned by the REST service in the default projection.
 */
@Resource
@ApiModel(value = "Events",
        description = "A set of immutable events")

public class EventsRepresentation {
    @EmbeddedResource("events")
    private Collection<EventRepresentation> events;

    @Link
    private HALLink self;

    public EventsRepresentation(List<Event> events, UriInfo uriInfo) {
        this.events = new ArrayList<>();
        this.events.addAll(events.stream()
                .map(event -> new EventRepresentation(event, uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(AccountEventServiceExposure.class)
                .build())
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "events",
            value = "the list of events.")
    public Collection<EventRepresentation> getEvents() {
        return Collections.unmodifiableCollection(events);
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the list of events itself.")
    public HALLink getSelf() {
        return self;
    }
}
