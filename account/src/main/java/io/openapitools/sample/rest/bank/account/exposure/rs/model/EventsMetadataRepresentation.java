package io.openapitools.sample.rest.bank.account.exposure.rs.model;

import javax.ws.rs.core.UriInfo;

import io.openapitools.sample.rest.bank.account.exposure.rs.AccountEventFeedMetadataServiceExposure;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A very simple metadata representation for Events
 *
 */
@Resource
@ApiModel(value = "EventsMetadata",
        description = "A very simple way of delivering metadata to the consumer of a service")
public class EventsMetadataRepresentation {

    private static final String DEFAULT = "{\"events\": {\n" +
            "           \"description\": \"This is a very simple non-persisted edition of the metadata for events on account\"," +
            "           \"purpose\": \"To show that it is easy to deliver information as part of the service and not only in the API docs\"" +
            "           \"supported-versions\": \"This is only in the current initial version 1\"" +
            "}}";

    @Link
    private HALLink self;

    private String metadata;

    /**
      * @param metadata must be formatted as valid JSON, rig now it is ignored and replaced with a static JSON document
     */
    public EventsMetadataRepresentation(String metadata, UriInfo uriInfo) {
        this.metadata = DEFAULT;
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(AccountEventFeedMetadataServiceExposure.class)
                .build())
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the reconciled transaction list.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "metadata",
            value = "json formatted description of relevant metadata")
    public String getMetadata() {
        return metadata;
    }

}
