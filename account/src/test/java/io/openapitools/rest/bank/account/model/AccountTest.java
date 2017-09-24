package io.openapitools.rest.bank.account.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.Test;

public class AccountTest {

    @Test
    public void testAddTransaction() {
        Account account = new Account("5479", "123456", "Savings account");
        account.addTransaction("description", new BigDecimal("1234.42"));

        assertEquals(1, account.getTransactions().size());
        Transaction transaction = account.getTransactions().iterator().next();
        assertEquals("description", transaction.getDescription());
        assertEquals(new BigDecimal("1234.42"), transaction.getAmount());
    }

    @Test
    public void testAddTransactionAndReconcile() {
        Account account = new Account("5479", "123456", "Savings account");
        account.addTransaction("description", new BigDecimal("1234.42"));

        assertEquals(1, account.getTransactions().size());
        Transaction transaction = account.getTransactions().iterator().next();
        assertEquals("description", transaction.getDescription());
        assertEquals(new BigDecimal("1234.42"), transaction.getAmount());
        account.addReconciledTransaction(transaction, true, "it is reconciled");
        assertEquals(1, account.getReconciledTransactions().size());
        Set<ReconciledTransaction> ts = account.getReconciledTransactions();
        ReconciledTransaction rtx = ts.iterator().next();
        assertEquals(true, rtx.getReconciled());
        assertEquals("it is reconciled", rtx.getNote());
    }

    @Test
    public void testAddTransactionAndNonReconciled() {
        Account account = new Account("5479", "123456", "Savings account");
        account.addTransaction("description", new BigDecimal("1234.42"));

        assertEquals(1, account.getTransactions().size());
        Transaction transaction = account.getTransactions().iterator().next();
        assertEquals("description", transaction.getDescription());
        assertEquals(new BigDecimal("1234.42"), transaction.getAmount());
        account.addReconciledTransaction(transaction, false, "it is not reconciled");
        assertEquals(1, account.getReconciledTransactions().size());
        Set<ReconciledTransaction> ts = account.getReconciledTransactions();
        ReconciledTransaction rtx = ts.iterator().next();
        assertEquals(false, rtx.getReconciled());
        assertEquals("it is not reconciled", rtx.getNote());
    }
}
