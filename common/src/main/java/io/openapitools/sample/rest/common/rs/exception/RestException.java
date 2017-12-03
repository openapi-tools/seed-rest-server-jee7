package io.openapitools.sample.rest.common.rs.exception;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.openapitools.sample.rest.common.rs.error.StandardErrorCodes;

/**
 * Rest exception always including an error (reason) code.
 * <p>
 * The exception will be mapped to a HTTP status code 500.
 */
@javax.ejb.ApplicationException
public class RestException extends RuntimeException {

    private static final long serialVersionUID = -3554651193684565097L;
    private StandardErrorCodes error;

    private final Map<String, String> values = new ConcurrentHashMap<>();
    private final Object details;

    public RestException(StandardErrorCodes error) {
        super(error.getMessageTemplate());
        this.error = error;
        this.details = null;
    }

    /**
     * @return the substitution values for the exception.
     */
    public Map<String, String> getSubstitutionValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * @return the error code of the exception.
     */
    public StandardErrorCodes getErrorCode() {
        return error;
    }

    public Object getDetails() {
        return details;
    }
}
