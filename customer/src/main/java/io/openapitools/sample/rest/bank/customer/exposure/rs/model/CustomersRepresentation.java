package io.openapitools.sample.rest.bank.customer.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import io.openapitools.sample.rest.bank.customer.exposure.rs.CustomerServiceExposure;
import io.openapitools.sample.rest.bank.customer.model.Customer;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a set of customers from the REST service exposure in this default projection.
 */
@Resource
@ApiModel(value = "Customers",
        description = "a list of Customers in default projection")
public class CustomersRepresentation {

    @Link
    private HALLink self;

    @EmbeddedResource("customers")
    private Collection<CustomerRepresentation> customers;

    public CustomersRepresentation(List<Customer> customers, UriInfo uriInfo) {
        this.customers = new ArrayList<>();
        this.customers.addAll(customers.stream().map(customer -> new CustomerRepresentation(customer, uriInfo)).collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(CustomerServiceExposure.class)
            .build())
            .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the customer list itself.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "customers",
            value = "customers list.")
    public Collection<CustomerRepresentation> getCustomers() {
        return Collections.unmodifiableCollection(customers);
    }
}
