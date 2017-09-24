package io.openapitools.rest.bank.customer.exposure.rs.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The necessary input for creation of an Customer and used for updating an Customer.
 */
@ApiModel(value = "CustomerUpdate",
        description = "the inout necessary for creating an Customer")

public class CustomerUpdateRepresentation {

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


    @ApiModelProperty(
            access = "public",
            name = "firstName",
            required = true,
            example = "Ole",
            value = "the first name of af customer")
    public String getFirstName() {
        return firstName;
    }

    @ApiModelProperty(
            access = "public",
            name = "middleName",
            required = true,
            example = "Valser",
            value = "the customers middle name")
    public String getMiddleName() {
        return middleName;
    }

    @ApiModelProperty(
            access = "public",
            name = "sirname",
            required = true,
            example = "Hansen",
            value = "the sirname of the customer.",
            notes = " the last name for a customer")
    public String getSirname() {
        return sirname;
    }

    @ApiModelProperty(
            access = "public",
            name = "number",
            required = true,
            example = "1234567890",
            value = "the customer number.",
            notes = " the identifier for a customer")
    public String getNumber() {
        return number;
    }


}
