package io.openapitools.sample.rest.common.rs.jersey;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * JAX-RS feature implementation make sure that the default Jersey MOXy provider is disabled.
 */
public class DisableJerseyMOXyFeature implements Feature {
    @Override
    public boolean configure(FeatureContext ctx) {
        ctx.property("jersey.config.disableMoxyJson", "true");
        return true;
    }
}
