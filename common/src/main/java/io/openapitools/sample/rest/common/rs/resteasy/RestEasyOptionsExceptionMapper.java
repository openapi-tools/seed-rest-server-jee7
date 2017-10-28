package io.openapitools.sample.rest.common.rs.resteasy;

import javax.ws.rs.core.Response;

/**
 * Ensure that we do not interfere with RestEasy options handling.
 *
 * See RestEasy
 * <a href="https://docs.jboss.org/resteasy/docs/3.0.9.Final/userguide/html_single/#ExceptionHandling">documentation</a>
 * for additional information.
 */
public class RestEasyOptionsExceptionMapper {


    public Response toResponse() {
        return null;
    }

}
