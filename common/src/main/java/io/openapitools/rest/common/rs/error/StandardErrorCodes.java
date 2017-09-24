package io.openapitools.rest.common.rs.error;

import javax.ws.rs.core.Response;

/**
 * Standard/common error codes used by NIC and to convenience for the application developer.
 * Also serves as a example for how to implement a enumeration of error code (something any service application must have).
 */
public enum StandardErrorCodes {

    /**
     * Used for Bean constraint validation errors e.g. {@link javax.validation.ConstraintViolation}
     * Should not be used by applications.
     */
    CONSTRAINT_VALIDATION(Response.Status.BAD_REQUEST, "Following constraints failed validation: ${failedConstraints}"),

    /**
     * Used by JSON deserializer, when String value cannot be converted to a target type due to invalid format.
     */
    INVALID_FORMAT(Response.Status.BAD_REQUEST, "${invalidFormatError}"),

    /**
     * Used by NIF when unable to determine the causer of an error.
     * Should not be used by applications.
     */
    UNCATEGORIZED_ERROR(Response.Status.INTERNAL_SERVER_ERROR, "Internal error occurred. Error is logged"),

    /**
     * Used when the request is malformed and the parser fails.
     * Should not be used by applications.
     */
    JSON_MALFORMED(Response.Status.BAD_REQUEST, "The request is malformed. The parsing failed with the following message \"${failedJSON}\""),

    /**
     * Used when the request header does not contain X-Client-Version.
     * Should not be used by applications.
     */
    XCLIENT_MISSING(Response.Status.BAD_REQUEST, "Client must provide its version in 'X-Client-Version' header"),

    /**
     * Used when the requested resource could not be found.
     */
    RESOURCE_NOT_FOUND(Response.Status.NOT_FOUND, "The requested resource ${notFoundResource} could not be found"),

    /**
     * Used when OAuth credentials doesn't have authorization for accessing something.
     * E.g. caused by {@link javax.annotation.security.RolesAllowed}
     */
    USER_UNAUTHORIZED(Response.Status.UNAUTHORIZED, "You are not authorized to ${method} on ${unauthorizedResource}");

    private final String messageTemplate;
    private final Response.Status status;

    StandardErrorCodes(Response.Status status, String messageTemplate) {
        this.status = status;
        this.messageTemplate = messageTemplate;
    }


    public Response.Status getResponseStatus() {
        return status;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }


}
