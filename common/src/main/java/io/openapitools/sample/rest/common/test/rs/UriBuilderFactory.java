package io.openapitools.sample.rest.common.test.rs;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito "factory" to create UriBuilder instances.
 */
public class UriBuilderFactory implements Answer<UriBuilder> {
    private final URI baseUri;

    public UriBuilderFactory(URI baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public UriBuilder answer(InvocationOnMock invocation) {
        return UriBuilder.fromUri(baseUri);
    }
}
