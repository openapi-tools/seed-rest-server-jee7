package io.openapitools.sample.openapi;

import io.swagger.models.Operation;

/**
 * adds security schemes to open api doc
 */
public class SecuritySchemes {

    private SecuritySchemes() {
        //intentionally empty
    }

    public static void addStandardSecuritySchemes(Operation operation) {
        operation.setVendorExtension("x-standard-extension", "x-Standard-Extension-Value");
        operation.setVendorExtension("x-auth-roles", new io.swagger.models.SecurityScope("authenticated-by", "access_token"));
        operation.setVendorExtension("x-supports-scheme", new io.swagger.models.SecurityScope("identity", "jwt_token"));
    }

}
