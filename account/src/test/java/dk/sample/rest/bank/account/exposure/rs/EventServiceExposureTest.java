package dk.sample.rest.bank.account.exposure.rs;


import dk.sample.rest.bank.account.exposure.rs.model.EventRepresentation;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import dk.sample.rest.bank.account.exposure.rs.model.EventsRepresentation;
import dk.sample.rest.bank.account.model.Event;
import dk.sample.rest.bank.account.persistence.AccountArchivist;
import dk.sample.rest.common.test.rs.UriBuilderFactory;
import dk.sample.time.CurrentTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EventServiceExposureTest {

    @Mock
    AccountArchivist archivist;

    @InjectMocks
    AccountEventServiceExposure service;

    @Test
    public void testListEvents() throws URISyntaxException {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn("mockedID");
        when(event.getInformation()).thenReturn("event information");
        when(event.getOrigin()).thenReturn(new URI("/account-events/5479-123456/eventSID"));
        when(event.getSequence()).thenReturn(1234567L);

        List<Event> eventList = new ArrayList<>(3);
        eventList.add(new Event(new URI("accounts/5479-123456/users"),
                "default", CurrentTime.now()));
        eventList.add(new Event(new URI("accounts/5479-123456/cards"),
                "5479-123456-other", CurrentTime.now()));
        eventList.add(new Event(new URI("accounts/5479-123456/transactions/mockedTxSID"),
                "5479-123456", CurrentTime.nowAsZonedDateTime().minusDays(1).toInstant()));

        when(archivist.findEvents(Optional.empty()))
                .thenReturn(eventList);

        Response response = service.listAll(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765", "");
        EventsRepresentation events = (EventsRepresentation) response.getEntity();

        assertEquals(3, events.getEvents().size());
        assertEquals("http://mock/account-events", events.getSelf().getHref());

        Collection<EventRepresentation> ers = events.getEvents();
        int found = 0;
        for (EventRepresentation eventRepresentation : ers) {
            if("default".equals(eventRepresentation.getCategory())) {
                assertEquals("http://mock/accounts/5479-123456/users", eventRepresentation.getOrigin().getHref());
                assertEquals("default", eventRepresentation.getCategory());
                assertEquals("http://mock/account-events/default/" + eventRepresentation.getId(), eventRepresentation.getSelf().getHref());
                found++;
            }
            if("5479-123456-other".equals(eventRepresentation.getCategory())) {
                assertEquals("http://mock/accounts/5479-123456/cards", eventRepresentation.getOrigin().getHref());
                assertEquals("5479-123456-other", eventRepresentation.getCategory());
                assertEquals("http://mock/account-events/5479-123456-other/" + eventRepresentation.getId(), eventRepresentation.getSelf().getHref());
                found++;
            }
            if("5479-123456".equals(eventRepresentation.getCategory())) {
                assertEquals("http://mock/accounts/5479-123456/transactions/mockedTxSID", eventRepresentation.getOrigin().getHref());
                assertEquals("5479-123456", eventRepresentation.getCategory());
                assertEquals("http://mock/account-events/5479-123456/" +  eventRepresentation.getId(), eventRepresentation.getSelf().getHref());
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
        when(event.getOrigin()).thenReturn(new URI("account-events/5479-123456/eventSID"));
        when(event.getSequence()).thenReturn(1234567L);

        when(archivist.getEventsForCategory(Event.getCategory("5479", "123456"), Optional.empty()))
                .thenReturn(Collections.singletonList(new Event(new URI("account-events/5479-1234567/eventSID"),
                        "5479-123456", CurrentTime.now())));

        Response response = service.getByCategory(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "5479-123456", "");
        EventsRepresentation events = (EventsRepresentation) response.getEntity();

        assertEquals(1, events.getEvents().size());
        assertEquals("http://mock/account-events", events.getSelf().getHref());

        response = service.getByCategory(ui, request, "application/hal+json;no-real-type", "this-is-a-Log-Token-that-r0cks-98765",
            "5479-123456", "");
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
        when(event.getOrigin()).thenReturn(new URI("accounts/5479-123456/transactions/txSID"));
        when(event.getSequence()).thenReturn(1234567L);

        when(archivist.getEvent("5479-123456","eventSID"))
                .thenReturn(new Event(new URI("accounts/5479-1234567/transactions/txSID")));

        Response response = service.getSingle(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "5479-123456", "eventSID");
        EventRepresentation er = (EventRepresentation) response.getEntity();

        assertEquals("http://mock/accounts/5479-1234567/transactions/txSID", er.getOrigin().getHref());
        assertEquals("default", er.getCategory());
        assertEquals("http://mock/account-events/default/" + er.getId(), er.getSelf().getHref());

        response = service.getSingle(ui, request, "application/hal+json;no-real-type", "this-is-a-Log-Token-that-r0cks-98765",
            "5479-123456", "eventSID");
        assertEquals(415,response.getStatus());

    }

}
