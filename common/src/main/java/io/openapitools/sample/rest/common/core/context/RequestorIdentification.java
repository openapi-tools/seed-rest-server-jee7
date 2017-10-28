package io.openapitools.sample.rest.common.core.context;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Holds information to identifying the requestor instance that originated
 * the current invocation.
 * <p>
 * Care must be taken when changing this class that serialization of older
 * versions still work.</p>
 * <p>Instances of this is immutable!</p>
 */
public class RequestorIdentification implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * @serial Contains a string uniquely identifying the requesting client
     */
    private final String requestorId;

    /**
     * @serial Contains a string with an unique id of the request
     */
    private final String requestUID;

    /**
     * @serial Contains a timestamp of the request start time
     */
    private final Date requestTime;

    public RequestorIdentification(String requestorId) {
        requestUID = UUID.randomUUID().toString();
        requestTime = new Date();
        this.requestorId = requestorId;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public String getRequestUID() {
        return requestUID;
    }

    public Date getRequestTime() {
        return new Date(requestTime.getTime());
    }

    public String toString() {
        return "requestorId[" + requestorId + "], requestorUID[" + requestUID + "], requestTime[" + requestTime + "]";
    }
}
