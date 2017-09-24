package io.openapitools.rest.bank.customer.exposure.rs;

import io.openapitools.rest.bank.customer.exposure.rs.model.CustomersRepresentation;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openapitools.rest.bank.customer.exposure.rs.model.CustomerRepresentation;
import io.openapitools.rest.bank.customer.exposure.rs.model.CustomerUpdateRepresentation;
import io.openapitools.rest.bank.customer.model.Customer;
import io.openapitools.rest.bank.customer.persistence.CustomerArchivist;
import io.openapitools.rest.common.test.rs.UriBuilderFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CustomerServiceExposureTest {

    @Mock
    CustomerArchivist archivist;

    @InjectMocks
    CustomerServiceExposure service;

    @Test
    public void testList() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.listCustomers())
            .thenReturn(Arrays.asList(new Customer("Hans", "Peter", "Hansen"), new Customer("Anders", "P", "Dinesen")));

        Response response = service.list(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765");
        CustomersRepresentation customers = (CustomersRepresentation) response.getEntity();

        assertEquals(2, customers.getCustomers().size());
        assertEquals("http://mock/customers", customers.getSelf().getHref());

        response = service.list(ui, request, "application/hal+json;concept=non.existing;type","this-is-a-Log-Token-that-r0cks-98765");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testGet() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.getCustomer("1234567890")).thenReturn(new Customer("Hans", "Peter", "Hansen"));

        CustomerRepresentation customer = (CustomerRepresentation) service.get(ui, request, "1234567890", "application/hal+json",
            "this-is-a-Log-Token-that-r0cks-98765").getEntity();

        assertEquals("Hans", customer.getFirstName());
        assertEquals("Peter", customer.getMiddleName());
        assertEquals("Hansen", customer.getSirname());
        assertEquals("http://mock/customers/"+customer.getNumber(), customer.getSelf().getHref());

        Response response = service.get(ui, request, "1234567890", "application/hal+json;concept=customer;v=0",
            "this-is-a-Log-Token-that-r0cks-98765");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testCreate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        CustomerUpdateRepresentation customerUpdate = mock(CustomerUpdateRepresentation.class);
        when(customerUpdate.getFirstName()).thenReturn("Walther");
        when(customerUpdate.getMiddleName()).thenReturn("Gunnar");
        when(customerUpdate.getSirname()).thenReturn("Olesen");
        when(customerUpdate.getNumber()).thenReturn("1234567890");

        when(archivist.findCustomer("1234567890")).thenReturn(Optional.empty());

        CustomerRepresentation resp = (CustomerRepresentation) service.createOrUpdate(ui, request, "1234567890",
            "this-is-a-Log-Token-that-r0cks-98765", customerUpdate).getEntity();

        assertEquals("Olesen", resp.getSirname());
        assertEquals("Walther", resp.getFirstName());
        assertEquals("Gunnar", resp.getMiddleName());
        assertEquals("http://mock/customers/" + resp.getNumber(), resp.getSelf().getHref());
    }

    @Test
    public void testUpdate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        Customer customer = new Customer("Gurli", "Lise", "Jensen");

        CustomerUpdateRepresentation customerUpdate = mock(CustomerUpdateRepresentation.class);
        when(customerUpdate.getSirname()).thenReturn("Jensen");
        when(customerUpdate.getFirstName()).thenReturn("Gurli");
        when(customerUpdate.getMiddleName()).thenReturn("Lise");
        when(customerUpdate.getNumber()).thenReturn("1234567890");

        when(archivist.findCustomer("1234567890")).thenReturn(Optional.of(customer));

        CustomerRepresentation resp = (CustomerRepresentation) service.createOrUpdate(ui, request, "1234567890",
            "this-is-a-Log-Token-that-r0cks-98765", customerUpdate).getEntity();

        assertEquals(customer.getSirname(), customer.getSirname());
        assertEquals(customer.getMiddleName(), resp.getMiddleName());
        assertEquals(customer.getFirstName(), resp.getFirstName());
        assertEquals(customer.getSid(), resp.getNumber());

        assertEquals("http://mock/customers/" + resp.getNumber(), resp.getSelf().getHref());
    }

    @Test(expected = WebApplicationException.class)
    public void testCreateInvalidRequest() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);

        CustomerUpdateRepresentation customerUpdate = mock(CustomerUpdateRepresentation.class);
        when(customerUpdate.getFirstName()).thenReturn("Hans");

        service.createOrUpdate(ui, request, "1234567890", "this-is-a-Log-Token-that-r0cks-98765", customerUpdate);
        fail("Should have thrown exception before this step");
    }
}
