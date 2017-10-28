package io.openapitools.sample.rest.bank.account.exposure.rs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.openapitools.sample.rest.bank.account.exposure.rs.model.TransactionRepresentation;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openapitools.api.capabilities.Sort;
import io.openapitools.sample.rest.bank.account.exposure.rs.model.TransactionsRepresentation;
import io.openapitools.sample.rest.bank.account.model.Account;
import io.openapitools.sample.rest.bank.account.model.Transaction;
import io.openapitools.sample.rest.bank.account.persistence.AccountArchivist;
import io.openapitools.sample.rest.common.test.rs.UriBuilderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TransactionServiceExposureTest {

    @Mock
    AccountArchivist archivist;

    @InjectMocks
    TransactionServiceExposure service;

    @Test
    public void testList() {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Account account = mock(Account.class);
        when(account.getRegNo()).thenReturn("5479");
        when(account.getAccountNo()).thenReturn("123456");
        List<Sort> sort = Collections.emptyList();
        when(archivist.getTransactions("5479", "123456", Optional.empty(), Optional.empty(), sort)).thenReturn(
                Collections.singletonList(new Transaction(account, new BigDecimal("1234.42"), "description")));

        Response response = service.list(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "5479", "123456", "", "", "");
        TransactionsRepresentation transactions = (TransactionsRepresentation) response.getEntity();

        assertEquals(1, transactions.getTransactions().size());
        assertEquals("http://mock/accounts/5479-123456/transactions", transactions.getSelf().getHref());

        response = service.list(ui, request, "application/hal+json;concept=non.existing;type", "this-is-a-Log-Token-that-r0cks-98765"
            ,"5479", "123456",
                "", "", "");
        assertEquals(415,response.getStatus());
    }

    @Test
    public void testGet() {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Account account = mock(Account.class);
        when(account.getRegNo()).thenReturn("5479");
        when(account.getAccountNo()).thenReturn("123456");
        Transaction dbTransaction = new Transaction(account, new BigDecimal("1234.42"), "description");
        when(archivist.getTransaction("5479", "123456", "xxx-yyy")).thenReturn(dbTransaction);

        Response response = service.get(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765"
            ,"5479", "123456", "xxx-yyy");
        TransactionRepresentation transaction = (TransactionRepresentation) response.getEntity();

        assertEquals("1234.42", transaction.getAmount());
        assertEquals("http://mock/accounts/5479-123456/transactions/" + dbTransaction.getId(), transaction.getSelf().getHref());

        response = service.get(ui, request, "application/hal+json;concept=non.existing;type", "this-is-a-Log-Token-that-r0cks-98765",
            "5479", "123456", "xxx-yyy");
        assertEquals(415,response.getStatus());
    }

    @Test
    public void testCreate() {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);

        Account account = mock(Account.class);
        when(account.getRegNo()).thenReturn("5479");
        when(account.getAccountNo()).thenReturn("123456");
        Transaction dbTransaction = new Transaction("human-readable-semantic-identifier", account, new BigDecimal("1234.42"), "a description");
        when(archivist.getTransaction("5479", "123456", "xxx-yyy")).thenReturn(dbTransaction);

        Response response = service.get(ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "5479", "123456", "xxx-yyy");
        TransactionRepresentation transaction = (TransactionRepresentation) response.getEntity();

        assertEquals("1234.42", transaction.getAmount());
        assertEquals("http://mock/accounts/5479-123456/transactions/human-readable-semantic-identifier", transaction.getSelf().getHref());
    }

}
