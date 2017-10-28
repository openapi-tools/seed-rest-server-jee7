package io.openapitools.sample.rest.common.rs.context;

import javax.servlet.http.HttpServletRequest;

import io.openapitools.sample.rest.common.core.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a request expected to carry context information.
 */
public abstract class ContextualRequest {
    public abstract void populateContext(Context ctx);

    /**
     * Factory to create the correct instance, e.g., static based context.
     */
    public static class Factory {
        private static final Logger LOG = LoggerFactory.getLogger(Factory.class);

        private final Context stubbedContext;

        public Factory(Context stubbedContext) {
            this.stubbedContext = stubbedContext;
        }

        public ContextualRequest getContextualRequest(HttpServletRequest request) {
            return new StubbedContextualRequest(request, stubbedContext);

        }
    }
}
