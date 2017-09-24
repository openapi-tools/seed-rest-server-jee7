package io.openapitools.rest.common.rs.filter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * Setting up headers for cross domain http requests (CORS).
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class OriginFilter implements ContainerResponseFilter {

    public static final Map<String, String> HEADERS = new HashMap<String, String>() {
        {
            put("Access-Control-Allow-Origin", "*");
            put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
            put("Access-Control-Allow-Headers", "Content-Type, Accept, X-Log-Token, X-Client-Version, Authorization");
            put("Access-Control-Expose-Headers", "Location, X-Log-Token");
        }
    };

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        addOriginHeaders(response.getHeaders());
    }

    private static void addOriginHeaders(MultivaluedMap<String, Object> headers) {
        for (Map.Entry<String, String> header : HEADERS.entrySet()) {
            headers.putSingle(header.getKey(), header.getValue());
        }
    }
}
