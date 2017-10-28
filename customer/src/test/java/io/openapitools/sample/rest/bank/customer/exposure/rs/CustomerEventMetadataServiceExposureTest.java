package io.openapitools.sample.rest.bank.customer.exposure.rs;


import io.openapitools.sample.rest.bank.customer.exposure.rs.model.EventsMetadataRepresentation;
import io.openapitools.sample.rest.bank.customer.persistence.CustomerArchivist;
import java.net.URI;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.openapitools.sample.rest.common.test.rs.UriBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CustomerEventMetadataServiceExposureTest {

    @Mock
    CustomerArchivist archivist;

    @InjectMocks
    CustomerEventFeedMetadataServiceExposure service;

    @Test
    public void testMetadata(){
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        Request request = mock(Request.class);
        Response response = service.getMetadata(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765");
        EventsMetadataRepresentation info = (EventsMetadataRepresentation) response.getEntity();
        assertNotNull(info);
        assertTrue(info.getMetadata().contains("purpose"));
        assertEquals("http://mock/customer-events-metadata", info.getSelf().getHref());

        response = service.getMetadata(ui, request, "application/hal+json;concept=metadata", "this-is-a-Log-Token-that-r0cks-98765");
        assertEquals(415,response.getStatus());
    }

    @Test
    public void testVersionedMetadata(){
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        Request request = mock(Request.class);
        Response response = service.getMetadata(ui, request, "application/hal+json;concept=metadata;v=1",
            "this-is-a-Log-Token-that-r0cks-98765");
        EventsMetadataRepresentation info = (EventsMetadataRepresentation) response.getEntity();
        assertNotNull(info);
        assertTrue(info.getMetadata().contains("purpose"));
        assertEquals("http://mock/customer-events-metadata", info.getSelf().getHref());
    }

}
