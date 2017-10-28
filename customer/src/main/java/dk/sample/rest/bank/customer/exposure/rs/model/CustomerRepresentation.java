package dk.sample.rest.bank.customer.exposure.rs.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.UriInfo;

import dk.sample.rest.bank.customer.exposure.rs.CustomerServiceExposure;
import dk.sample.rest.bank.customer.model.Customer;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a single Customer as returned from REST service in the default projection.
 */
@Resource
@ApiModel(value = "Customer",
        description = "the Customer")
public class CustomerRepresentation {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z]{2,40}")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Z]{2,40}")
    private String middleName;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z]{2,40}")
    private String sirname;

    @NotNull
    @Pattern(regexp = "^[0-9]{10}")
    private String number;

    @Link
    private HALLink self;

    public CustomerRepresentation(Customer customer, UriInfo uriInfo) {
        this.firstName = customer.getFirstName();
        this.middleName = customer.getMiddleName();
        this.sirname = customer.getSirname();
        this.number = customer.getSid();
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
            .path(CustomerServiceExposure.class)
            .path(CustomerServiceExposure.class, "get")
            .build(customer.getSid()))
            .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "firstName",
            example = "Birgit",
            value = "the first name.")
    public String getFirstName() {
        return firstName;
    }

    @ApiModelProperty(
            access = "public",
            name = "middleName",
            example = "Wolthers",
            value = "the middle name.")
    public String getMiddleName() {
        return middleName;
    }

    @ApiModelProperty(
            access = "public",
            name = "sirname",
            example = "Hansen",
            value = "the sirname.")
    public String getSirname() {
        return sirname;
    }

    @ApiModelProperty(
            access = "public",
            name = "number",
            example = "Hansen",
            value = "the customer identifier.")
    public String getNumber() {
        return number;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the customer itself.")
    public HALLink getSelf() {
        return self;
    }
}
