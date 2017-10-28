package io.openapitools.sample.rest.bank.account.exposure.rs;

import io.openapitools.sample.rest.bank.account.exposure.rs.model.ReconciledTransactionRepresentation;
import io.openapitools.sample.rest.bank.account.exposure.rs.model.ReconciledTransactionsRepresentation;
import io.openapitools.sample.rest.bank.account.model.Account;
import io.openapitools.sample.rest.bank.account.model.ReconciledTransaction;
import io.openapitools.sample.rest.bank.account.model.Transaction;
import io.openapitools.sample.rest.bank.account.persistence.AccountArchivist;
import io.openapitools.sample.rest.common.test.rs.UriBuilderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReconciledTransactionServiceExposureTest {

    @Mock
    AccountArchivist archivist;

    @InjectMocks
    ReconciledTransactionServiceExposure service;

    @Test
    public void testList() {
        UriInfo ui = mock(UriInfo.class);
        when(ui.getBaseUriBuilder()).then(new UriBuilderFactory(URI.create("http://mock")));

        Request request = mock(Request.class);
        Account account = mock(Account.class);

        when(account.getRegNo()).thenReturn("5479");
        when(account.getAccountNo()).thenReturn("123456");

        Transaction tx= new Transaction("human-readable-sid", account, new BigDecimal("1234.42"), "description");
        ReconciledTransaction rtx = new ReconciledTransaction(true,"This is a note", tx);
        when(account.getReconciledTransactions())
                .thenReturn(new HashSet<>(Collections.singletonList(rtx)));
        when(archivist.getAccount("5479", "123456")).thenReturn(account);

        Response response = service.list( ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "5479", "123456");
        ReconciledTransactionsRepresentation reconciledTxs = (ReconciledTransactionsRepresentation) response.getEntity();

        assertEquals(1, reconciledTxs.getReconciledTransactions().size());
        assertEquals("http://mock/accounts/5479-123456/reconciled-transactions", reconciledTxs.getSelf().getHref());

        response = service.list( ui, request, "application/hal+json;concept=non.existing;type",
            "this-is-a-Log-Token-that-r0cks-98765", "5479", "123456");
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
        Transaction tx = new Transaction(account,new BigDecimal("123.45"), "not much");
        ReconciledTransaction rtx = new ReconciledTransaction(true, "mocked decorated transaction", tx);
        when(archivist.getReconciledTransaction("5479", "123456", "xxx-yyy")).thenReturn(rtx);

        Response response = service.get( ui, request, "application/hal+json", "this-is-a-Log-Token-that-r0cks-98765",
            "5479", "123456", "xxx-yyy");
        ReconciledTransactionRepresentation reconciledTx = (ReconciledTransactionRepresentation) response.getEntity();

        assertEquals("mocked decorated transaction", reconciledTx.getNote());
        assertEquals(true, reconciledTx.getReconciled());
        assertEquals("http://mock/accounts/5479-123456/reconciled-transactions/" + tx.getId(), reconciledTx.getSelf().getHref());
        assertEquals("http://mock/accounts/5479-123456/transactions/" + tx.getId(), reconciledTx.getTransaction().getHref());

        response = service.get( ui, request, "application/hal+json;concept=non.existing;type", "this-is-a-Log-Token-that-r0cks-98765",
            "5479", "123456", "xxx-yyy");
        assertEquals(415,response.getStatus());
    }

}
