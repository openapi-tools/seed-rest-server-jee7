package io.openapitools.sample.rest.common.rs.context;

import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import io.openapitools.sample.rest.common.core.context.Context;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context information stubbed for development purposes.
 */
public class StubbedContextualRequest extends ContextualRequest {

    private static final Logger LOG = LoggerFactory.getLogger(StubbedContextualRequest.class);

    private final HttpServletRequest request;
    private final Context stubbedContext;

    public StubbedContextualRequest(HttpServletRequest request, Context stubbedContext) {
        this.request = request;
        this.stubbedContext = stubbedContext;
    }

    @Override
    public void populateContext(Context context) {
        context.setUser(stubbedContext.getUser());
        //todo context.setRequestor(new RequestorIdentification(request.getRemoteAddr()));
        stubbedContext.getDeployableList().forEach(context::addDeployable);
        authenticate();
    }

    private boolean isAuthenticated() {
        return request.getUserPrincipal() != null || request.getAuthType() != null || request.getRemoteUser() != null;
    }

    private boolean isSameUserPrincipal(String user) {
        return request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(user);
    }

    protected void authenticate() {
        if (request.getHeader("authorization") != null
                && request.getHeader("authorization").startsWith("Bearer")) {
            try {
                String authHeader = request.getHeader("authorization");
                String decoded = new String(Base64.decodeBase64(authHeader.substring(7)), "ISO-8859-1");
                String[] credentials = decoded.split(":");
                if (credentials.length != 2) {
                    throw new BadRequestException("Authorization header existed with type 'Bearer' but contained no user/pass pair");
                }
                if (isAuthenticated()) {
                    if (!isSameUserPrincipal(credentials[0])) {
                        request.logout();
                        request.login(credentials[0], credentials[1]);
                    }
                } else {
                    request.login(credentials[0], credentials[1]);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unable to find charset [ISO-8859-1]", e);
            } catch (ServletException e) {
                String msg = "Unable to authenticate user";
                LOG.error(msg, e);
                throw new BadRequestException(msg);
            }
        }
    }
}
