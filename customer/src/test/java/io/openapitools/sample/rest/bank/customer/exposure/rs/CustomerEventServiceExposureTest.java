package io.openapitools.sample.rest.bank.customer.exposure.rs;


import io.openapitools.sample.rest.bank.customer.persistence.CustomerArchivist;
import io.openapitools.sample.rest.bank.customer.exposure.rs.model.EventRepresentation;
import io.openapitools.sample.rest.bank.customer.model.Event;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import io.openapitools.sample.rest.bank.customer.exposure.rs.model.EventsRepresentation;
import io.openapitools.sample.rest.common.test.rs.UriBuilderFactory;
import io.openapitools.sample.time.CurrentTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CustomerEventServiceExposureTest {

    @Mock
    CustomerArchivist archivist;

    @InjectMocks
    CustomerEventServiceExposure service;

    @Test
    public void testListEvents() throws URISyntaxException {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn("mockedID");
        when(event.getInformation()).thenReturn("event information");
        when(event.getOrigin()).thenReturn(new URI("/customer-events/1234567890/eventSID"));
        when(event.getSequence()).thenReturn(1234567L);

        List<Event> eventList = new ArrayList<>(3);
        eventList.add(new Event(new URI("customers/default/eventSID"),
                "default", CurrentTime.now()));
        eventList.add(new Event(new URI("customers/1234567890/cards"),
                "black-card", CurrentTime.now()));
        eventList.add(new Event(new URI("customers/1234567890/accounts"),
                "additional-account", CurrentTime.nowAsZonedDateTime().minusDays(1).toInstant()));

        when(archivist.findEvents(Optional.empty()))
                .thenReturn(eventList);

        Response response = service.listAll(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765", "");
        EventsRepresentation events = (EventsRepresentation) response.getEntity();

        assertEquals(3, events.getEvents().size());
        assertEquals("http://mock/customer-events", events.getSelf().getHref());

        Collection<EventRepresentation> ers = events.getEvents();
        int found = 0;
        for (EventRepresentation eventRepresentation : ers) {
            if("default".equals(eventRepresentation.getCategory())) {
                assertEquals("http://mock/customers/default/eventSID", eventRepresentation.getOrigin().getHref());
                assertEquals("default", eventRepresentation.getCategory());
                assertEquals("http://mock/customer-events/default/" + eventRepresentation.getId(), eventRepresentation.getSelf().getHref());
                found++;
            }
            if("black-card".equals(eventRepresentation.getCategory())) {
                assertEquals("http://mock/customers/1234567890/cards", eventRepresentation.getOrigin().getHref());
                assertEquals("black-card", eventRepresentation.getCategory());
                assertEquals("http://mock/customer-events/black-card/" + eventRepresentation.getId(), eventRepresentation.getSelf().getHref());
                found++;
            }
            if("additional-account".equals(eventRepresentation.getCategory())) {
                assertEquals("http://mock/customers/1234567890/accounts", eventRepresentation.getOrigin().getHref());
                assertEquals("additional-account", eventRepresentation.getCategory());
                assertEquals("http://mock/customer-events/additional-account/" +  eventRepresentation.getId(), eventRepresentation.getSelf().getHref());
                found++;
            }
        }
        assertEquals(3, found);

        response = service.listAll(ui, request, "application/hal+json;no-real-type", "this-is-a-Log-Token-that-r0cks-98765", "");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testListEventsByCategory() throws URISyntaxException {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Event event = mock(Event.class);
        when(event.getInformation()).thenReturn("event information");
        when(event.getOrigin()).thenReturn(new URI("customers-events/some-category/eventSID"));
        when(event.getSequence()).thenReturn(1234567L);

        when(archivist.getEventsForCategory(Event.getCategory("some", "category"), Optional.empty()))
                .thenReturn(Collections.singletonList(new Event(new URI("customer-events/some-category/eventSID"),
                        "some-category", CurrentTime.now())));

        Response response = service.getByCategory(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "some-category", "");
        EventsRepresentation events = (EventsRepresentation) response.getEntity();

        assertEquals(1, events.getEvents().size());
        assertEquals("http://mock/customer-events", events.getSelf().getHref());

        response = service.getByCategory(ui, request, "application/hal+json;no-real-type", "this-is-a-Log-Token-that-r0cks-98765",
            "some-category", "");
        assertEquals(415,response.getStatus());
    }

    @Test
    public void testGetEvent() throws URISyntaxException {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn("eventSID");
        when(event.getInformation()).thenReturn("event information");
        when(event.getOrigin()).thenReturn(new URI("customers/1010101010/cards"));
        when(event.getSequence()).thenReturn(1234567L);

        when(archivist.getEvent("new-card","eventSID"))
                .thenReturn(new Event(new URI("customers/1010101010/cards")));

        Response response = service.getSingle(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "new-card", "eventSID");
        EventRepresentation er = (EventRepresentation) response.getEntity();

        assertEquals("default", er.getCategory());
        assertEquals("http://mock/customer-events/default/" + er.getId(), er.getSelf().getHref());
        assertEquals("http://mock/customers/1010101010/cards", er.getOrigin().getHref());

        response = service.getSingle(ui, request, "application/hal+json;no-real-type", "this-is-a-Log-Token-that-r0cks-98765",
            "5479-123456", "eventSID");
        assertEquals(415,response.getStatus());

    }

}
