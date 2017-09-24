package io.openapitools.rest.common.core.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Alternative;

/**
 * Represents the context of the current invocation in terms of user and application
 * data for the invocation.
 */
@Alternative
public class Context implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * @serial Invoking user
     */
    private CallerIdentification user;

    /**
     * @serial List of applications
     */
    private final List<Deployable> depList = new ArrayList<>();

    /**
     * @serial proxy information
     */
    private CallerIdentification proxy;

    /**
     * Creating empty context.
     */
    public Context() {
        // Instance for populating through the setters...
    }

    public Context(CallerIdentification user) {
        if (user == null) {
            throw new IllegalArgumentException("'user' must be given");
        }
        this.user = user;
    }

    public CallerIdentification getUser() {
        return user;
    }

    public void setUser(CallerIdentification user) {
        if (this.user != null) {
            throw new IllegalArgumentException("User has already been set");
        }
        this.user = user;
    }


    public List<Deployable> getDeployableList() {
        return Collections.unmodifiableList(depList);
    }

    /**
     * @return The deployable which was first added, i.e., the initiator.
     */
    public Optional<Deployable> getFirstDeployable() {
        if (depList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(depList.get(0));
        }
    }

    /**
     * @return The deployable which was last added to the context, i.e., the most recent component handling the request.
     */
    public Optional<Deployable> getLastDeployable() {
        if (depList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(depList.get(depList.size() - 1));
        }
    }

    public void addDeployable(Deployable dep) {
        if (dep == null) {
            throw new IllegalArgumentException("'dep' cannot be null");
        }
        depList.add(dep);
    }

    public CallerIdentification getProxy() {
        return proxy;
    }

    public void setProxy(CallerIdentification proxy) {
        if (this.proxy != null) {
            throw new IllegalArgumentException("Proxy has already been set");
        }
        this.proxy = proxy;
    }

    public String toString() {
        return "user[" + user + "], depList[" + depList + "]";
    }

}
