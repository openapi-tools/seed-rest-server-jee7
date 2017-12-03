package io.openapitools.sample.rest.bank.customer.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CustomerTest {

    @Test
    public void testNewCustomer() {
        Customer customer = new Customer("Hans", "Peter", "Hansen", "1234567890");
        assertEquals("Hans", customer.getFirstName());
        assertEquals("Peter", customer.getMiddleName());
        assertEquals("Hansen", customer.getSirname());
        assertEquals("1234567890", customer.getSid());
    }
}
