package io.openapitools.sample.rest.common.rs.error;

import java.net.URI;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilderException;

import io.openapitools.sample.rest.common.core.diagnostic.DiagnosticContext;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Simple Error representation
 */
@Resource
@ApiModel(value = "Error", description = "a simple error representation")
public class ErrorRepresentation {
    private static final String TIME_SUBSTITUTION_KEY = "time";
    private static final String LOG_TOKEN_SUBSTITUTION_KEY = "logToken";
    private static final String DETAILS_KEY = "details";

    private String msg;
    private String sid;
    @Link
    private HALLink resource;
    @Link
    private Map<String, Object> values;

    public ErrorRepresentation() {
    }

    private ErrorRepresentation(Builder builder) {
        if (builder.error == null) {
            throw new IllegalArgumentException("An error code must be supplied");
        } else {
            this.values = new LinkedHashMap<>();
            String logToken = DiagnosticContext.getLogToken();
            this.values.put("time", builder.time);
            this.values.put("logToken", logToken);
            this.values.putAll(builder.values);
            this.sid = builder.error;
            if (builder.details != null) {
                this.values.put("details", builder.details);
            }

            this.resource = builder.resourceUri == null ? null : this.createResourceLink(builder.resourceUri);
            this.msg = builder.msg;
        }
    }

    @ApiModelProperty(
            access = "public",
            name = "message",
            example = "this error happened due to this situation",
            value = "the contents of an error in the body as information til API consumer(s).")
    public String getMessage() {
        return this.msg;
    }

    @ApiModelProperty(
            access = "public",
            name = "sid",
            value = "the semantic id of the error.")
    public String getSid() {
        return this.sid;
    }

    @ApiModelProperty(
            access = "public",
            name = "values",
            value = "a collection of validation issues which caused the error.")
    public Map<String, Object> getValues() {
        return this.values;
    }

    @ApiModelProperty(
            access = "public",
            name = "resource",
            notes = "link to the origin of the cause of the error.")
    public HALLink getResource() {
        return this.resource;
    }


    private HALLink createResourceLink(URI uri) throws IllegalArgumentException, UriBuilderException {
        return (new io.openapitools.jackson.dataformat.hal.HALLink.Builder(uri)).title("Link to failed resource").build();
    }

    /**
     * a simple builder for errors representation
     */
    public static class Builder {

        private String msg;
        private URI resourceUri;
        private Map<String, Object> values = new LinkedHashMap<>();
        private Date time = new Date();
        private String error;
        private Object details;

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder time(Date time) {
            this.time = new Date(time.getTime());
            return this;
        }

        public Builder resource(URI uri) {
            this.resourceUri = uri;
            return this;
        }

        public Builder value(String key, Object value) {
            this.values.put(key, value);
            return this;
        }

        public Builder details(Object details) {
            this.details = details;
            return this;
        }

        public ErrorRepresentation build() {
            return new ErrorRepresentation(this);
        }
    }
}
