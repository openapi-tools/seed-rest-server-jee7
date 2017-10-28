package io.openapitools.sample.rest.common.core.diagnostic;

/**
 * Interface to allow access to a log token that are to be used in a given context.
 */
public interface ContextInfo {

    String getLogToken();

    void setLogToken(String token);

}
