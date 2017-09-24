package io.openapitools.rest.bank.account.exposure.rs;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.openapitools.rest.bank.account.exposure.rs.model.AccountRepresentation;
import io.openapitools.rest.bank.account.exposure.rs.model.AccountUpdateRepresentation;
import io.openapitools.rest.bank.account.exposure.rs.model.AccountsRepresentation;
import io.openapitools.rest.bank.account.model.Account;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openapitools.rest.bank.account.persistence.AccountArchivist;
import io.openapitools.rest.common.test.rs.UriBuilderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AccountServiceExposureTest {

    @Mock
    AccountArchivist archivist;

    @InjectMocks
    AccountServiceExposure service;

    @Test
    public void testList() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.listAccounts())
            .thenReturn(Arrays.asList(new Account("5479", "1", "Checking account"), new Account("5479", "2", "Savings account")));

        Response response = service.list(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765");
        AccountsRepresentation accounts = (AccountsRepresentation) response.getEntity();

        assertEquals(2, accounts.getAccounts().size());
        assertEquals("http://mock/accounts", accounts.getSelf().getHref());

        response = service.list(ui, request, "application/hal+json;concept=non.existing;type", "this-is-a-Log-Token-that-r0cks-98765");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testGet() {
        Request request = mock(Request.class);

        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        when(archivist.getAccount("5479", "1234")).thenReturn(new Account("5479", "1234", "Savings account"));

        AccountRepresentation account = (AccountRepresentation) service.get(ui, request, "5479", "1234",
             "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765").getEntity();

        assertEquals("5479", account.getRegNo());
        assertEquals("1234", account.getAccountNo());
        assertEquals("http://mock/accounts/5479-1234", account.getSelf().getHref());

        Response response = service.get(ui, request, "5479", "1234",
            "application/hal+json;concept=account;v=0", "this-is-a-Log-Token-that-r0cks-98765");
        assertEquals(415,response.getStatus());

    }

    @Test
    public void testCreate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        AccountUpdateRepresentation accountUpdate = mock(AccountUpdateRepresentation.class);
        when(accountUpdate.getName()).thenReturn("new Account");
        when(accountUpdate.getRegNo()).thenReturn("5479");
        when(accountUpdate.getAccountNo()).thenReturn("12345678");

        when(archivist.findAccount("5479", "12345678")).thenReturn(Optional.empty());

        AccountRepresentation resp = (AccountRepresentation) service.createOrUpdate(ui, request, "5479", "12345678",
            "this-is-a-Log-Token-that-r0cks-98765", accountUpdate).getEntity();

        assertEquals("new Account", resp.getName());
        assertEquals("5479", resp.getRegNo());
        assertEquals("12345678", resp.getAccountNo());
        assertEquals("http://mock/accounts/5479-12345678", resp.getSelf().getHref());
    }

    @Test
    public void testUpdate() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));
        when(ui.getPath()).thenReturn("http://mock");

        Account existingAcc = new Account("5479", "12345678", "Savings account");
        when(archivist.findAccount("5479", "12345678")).thenReturn(Optional.of(existingAcc));

        AccountUpdateRepresentation accountUpdate = mock(AccountUpdateRepresentation.class);
        when(accountUpdate.getName()).thenReturn("new name");
        when(accountUpdate.getRegNo()).thenReturn("5479");
        when(accountUpdate.getAccountNo()).thenReturn("12345678");

        AccountRepresentation resp = (AccountRepresentation) service.createOrUpdate(ui, request, "5479", "12345678",
            "this-is-a-Log-Token-that-r0cks-98765", accountUpdate).getEntity();

        //name of the existing account should be updated
        assertEquals("new name", existingAcc.getName());
        assertEquals("new name", resp.getName());
        assertEquals("5479", resp.getRegNo());
        assertEquals("12345678", resp.getAccountNo());
        assertEquals("http://mock/accounts/5479-12345678", resp.getSelf().getHref());
    }

    @Test(expected = WebApplicationException.class)
    public void testCreateInvalidRequest() throws Exception {
        Request request = mock(Request.class);
        UriInfo ui = mock(UriInfo.class);

        AccountUpdateRepresentation accountUpdate = mock(AccountUpdateRepresentation.class);
        when(accountUpdate.getRegNo()).thenReturn("5479");
        when(accountUpdate.getAccountNo()).thenReturn("12345678");

        service.createOrUpdate(ui, request, "5479", "87654321",
            "this-is-a-Log-Token-that-r0cks-98765", accountUpdate);
        fail("Should have thrown exception before this step");
    }
}
