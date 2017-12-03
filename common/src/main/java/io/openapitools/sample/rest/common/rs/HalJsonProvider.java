package io.openapitools.sample.rest.common.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.openapitools.jackson.dataformat.hal.HALMapper;

/**
 * Specialization of the Jackson provider implementing {@link javax.ws.rs.ext.MessageBodyReader} and
 * {@link javax.ws.rs.ext.MessageBodyWriter} to ensure that serialization and deserialization is done in the same
 * way on different JAX-RS implementations. No wildcard type defined since this seems to have an issue with RESTeasy.
 * This specialization also sets up configuration of the Jackson mapper.
 */
@Provider
@Consumes({ "application/hal+json", MediaType.APPLICATION_JSON })
@Produces({ "application/hal+json", MediaType.APPLICATION_JSON })
public class HalJsonProvider extends JacksonJsonProvider {

    public HalJsonProvider() {
        this(new HALMapper());
    }

    public HalJsonProvider(ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        setMapper(mapper);
    }
}
