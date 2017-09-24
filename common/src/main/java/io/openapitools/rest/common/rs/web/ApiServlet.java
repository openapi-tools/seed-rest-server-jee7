package io.openapitools.rest.common.rs.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import io.openapitools.rest.common.rs.filter.OriginFilter;
import org.apache.commons.io.IOUtils;

/**
 * Responds with OpenAPI documentation JSON file created by Swagger Plugin
 * Used with the rest-service-archetype
 */
public class ApiServlet extends HttpServlet {

    private static final String SWAGGER_LOCATION = "/WEB-INF/api/swagger.json";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);

        try (InputStream swaggerDoc = getServletContext().getResourceAsStream(SWAGGER_LOCATION)) {
            IOUtils.copy(swaggerDoc, resp.getOutputStream());
        }
        for (Map.Entry<String, String> header : OriginFilter.HEADERS.entrySet()) {
            resp.setHeader(header.getKey(), header.getValue());
        }
    }
}
