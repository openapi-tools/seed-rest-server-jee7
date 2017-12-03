package io.openapitools.sample.rest.bank.account.exposure.rs.model;

import javax.ws.rs.core.UriInfo;

import io.openapitools.sample.rest.bank.account.exposure.rs.AccountEventServiceExposure;
import io.openapitools.sample.rest.bank.account.model.Event;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a single transaction in its default projection as returned by the REST service.
 */
@Resource
@ApiModel(value = "Event",
        description = "An immutable event")

public class EventRepresentation {
    private String id;
    private String time;
    private String sequence;
    private String category;

    @Link
    private HALLink self;

    @Link
    private HALLink origin;

    @Link
    private HALLink metadata;

    public EventRepresentation(Event event, UriInfo uriInfo) {
        this.id = event.getId();
        this.time = event.getTime().toString();
        this.sequence = event.getSequence().toString();
        this.category = event.getCategory();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(AccountEventServiceExposure.class)
                .path(AccountEventServiceExposure.class, "getSingle")
                .build(event.getCategory(), event.getId())).build();
        this.origin = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(event.getOrigin() != null ? event.getOrigin().getPath() : "no path")
                .build())
                .build();
        this.metadata = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(AccountEventServiceExposure.class)
                .build())
                .name("eventMetadata")
                .title("Metadata for Event Resource")
                .type("application/hal+json;concept=metadata")
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "id",
            value = "a semantic identifier for the event.")
    public String getId() {
        return id;
    }

    @ApiModelProperty(
            access = "public",
            name = "time",
            value = "the human readable time of when the event occurred.")
    public String getTime() {
        return time;
    }

    @ApiModelProperty(
            access = "public",
            name = "sequence",
            example = "1",
            value = "the sequence - in this example to show that time and sequence can be used for idempotency.")
    public String getSequence() {
        return sequence;
    }

    @ApiModelProperty(
            access = "public",
            name = "category",
            example = "1234-12345678",
            value = "the category - in which the event has been grouped into.",
            notes = "Default is (default) if no category has been set.")
    public String getCategory() {
        return category;
    }

    @ApiModelProperty(
            access = "public",
            name = "origin",
            notes = "link to the cause of the event - e.g. a link to a name change in account.")
    public HALLink getOrigin() {
        return origin;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "notes on the - link to the event itself.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "metadata",
            notes = "metadata about the event and feed.")
    public HALLink getMetadata() {
        return metadata;
    }

}
