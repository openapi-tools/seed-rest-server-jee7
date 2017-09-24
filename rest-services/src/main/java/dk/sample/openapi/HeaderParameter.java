package io.openapitools.openapi;

/**
 * ensuring that the headers are getting the String type in the openapi documentation
 */
public class HeaderParameter extends io.swagger.models.parameters.HeaderParameter {

    public HeaderParameter() {
        super.type("string");
    }
}
