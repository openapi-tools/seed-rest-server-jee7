package io.openapitools.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.openapitools.rest.bank.account.exposure.rs.AccountEventFeedMetadataServiceExposure;
import io.openapitools.rest.bank.account.exposure.rs.AccountEventServiceExposure;
import io.openapitools.rest.bank.account.exposure.rs.AccountServiceExposure;
import io.openapitools.rest.bank.account.exposure.rs.ReconciledTransactionServiceExposure;
import io.openapitools.rest.bank.account.exposure.rs.TransactionServiceExposure;
import io.openapitools.rest.bank.customer.exposure.rs.CustomerEventFeedMetadataServiceExposure;
import io.openapitools.rest.bank.customer.exposure.rs.CustomerEventServiceExposure;
import io.openapitools.rest.bank.customer.exposure.rs.CustomerServiceExposure;
import io.openapitools.rest.common.rs.JaxRsRuntime;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.OAuth2Definition;
import io.swagger.annotations.Scope;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Assembling the sample-rest-services application by including the relevant resource.
 */
@SwaggerDefinition(
    host = "sample.services.sample.dk",
    basePath = "/sample",
    produces = "application/hal+json",
    consumes = "application/json",
    schemes = SwaggerDefinition.Scheme.HTTPS,

    tags = {@Tag(name = "select", description = "select is an api capability see doc " +
        "on https://github.com/Nykredit/HATEOAS/blob/master/api-information/APICapabilities.md"),
        @Tag(name = "sort", description = "sort is an api capability see doc " +
            "on https://github.com/Nykredit/HATEOAS/blob/master/api-information/APICapabilities.md"),
        @Tag(name = "elements", description = "elements is an api capability see doc " +
            "on https://github.com/Nykredit/HATEOAS/blob/master/api-information/APICapabilities.md"),
        @Tag(name = "interval", description = "interval is an api capability see doc " +
            "on https://github.com/Nykredit/HATEOAS/blob/master/api-information/APICapabilities.md"),
        @Tag(name = "filter", description = "filter is an api capability see doc " +
            "on https://github.com/Nykredit/HATEOAS/blob/master/api-information/APICapabilities.md"),
        @Tag(name = "embed", description = "embed is an api capability see doc " +
            "on https://github.com/Nykredit/HATEOAS/blob/master/api-information/APICapabilities.md"),
        @Tag(name = "immutable", description = "immutable means the object never changes"),
        @Tag(name = "decorator", description = "decorator is an a pattern that allows other objects to" +
            " preserve their nature, e.g. stay immutable, stay authoritative etc.")},
    securityDefinition = @SecurityDefinition(
        apiKeyAuthDefinitions = {@ApiKeyAuthDefinition(name = "Authorization: Bearer",
            key = "Bearer",
            description = "A Bearer token",
            in = ApiKeyAuthDefinition.ApiKeyLocation.HEADER)},
        oAuth2Definitions = {
            @OAuth2Definition(
                description = "OAuth2 Implicit grant flow is used for every single endpoint",
                key = "oauth2",
                authorizationUrl = "https://banking.services.sample-bank.dk/token.oauth2?" +
                    "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=JWT",
                flow = OAuth2Definition.Flow.IMPLICIT,
                tokenUrl = "https://oauth.services.sample-bank.dk/token",
                scopes = {
                    @Scope(name = "read:default", description = "default access")
                }
            ),
            @OAuth2Definition(
                description = "OAuth2 Client Credentials grant flow is supported for every endpoint",
                key = "oauth2-cc",
                authorizationUrl = "https://banking.services.sample-bank.dk/token.oauth2?" +
                    "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=JWT",
                flow = OAuth2Definition.Flow.APPLICATION,
                tokenUrl = "https://oauth.services.sample-bank.dk/token",
                scopes = {
                    @Scope(name = "no:default", description = "default access")
                }
            ),
            @OAuth2Definition(
                description = "OAuth2 Authorization Code grant flow is supported for every endpoint",
                key = "oauth2-ac",
                authorizationUrl = "https://banking.services.sample-bank.dk/token.oauth2?" +
                    "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=JWT",
                flow = OAuth2Definition.Flow.ACCESS_CODE,
                tokenUrl = "https://oauth.services.sample-bank.dk/token",
                scopes = {
                    @Scope(name = "no:default", description = "default access")
                }
            ),
            @OAuth2Definition(
                description = "OAuth2 Resource Owner Password Credentials flow is supported for every endpoint",
                key = "oauth2-rop",
                authorizationUrl = "https://banking.services.sample-bank.dk/token.oauth2?" +
                    "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=JWT",
                flow = OAuth2Definition.Flow.PASSWORD,
                tokenUrl = "https://oauth.services.sample-bank.dk/token",
                scopes = {
                    @Scope(name = "no:default", description = "default access")
                }
            )
        }),
    externalDocs = @ExternalDocs(value = "documentation",
        url = "https://github.com/Nykredit/HATEOAS/tree/master/api-information")
    )

@ApplicationPath("/")
public class ServicesApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(Arrays.asList(
                AccountServiceExposure.class,
                TransactionServiceExposure.class,
                ReconciledTransactionServiceExposure.class,
                AccountEventServiceExposure.class,
                AccountEventFeedMetadataServiceExposure.class,
                CustomerServiceExposure.class,
                CustomerEventServiceExposure.class,
                CustomerEventFeedMetadataServiceExposure.class)
        );
        JaxRsRuntime.configure(classes);
        return classes;
    }
}
