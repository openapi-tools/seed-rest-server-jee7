package io.openapitools.sample.rest.common.persistence.jpa;

import java.sql.Timestamp;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Handling entity auditing by adding LAST_MODIFIED_BY, LAST_MODIFIED_TS to tables.
 * <p>
 * Note: It is assumed that it is possible to inject a context instance from CDI.
 * Note: Code has been omitted from this class in order to make it accessible on github
 *
 */
@MappedSuperclass
public class AbstractAuditable {

    /**
     * List of fields which is excluded from {@link #equals(Object)} and {@link #hashCode()} by default.
     */
    private static final String[] DEFAULT_EXCLUDED_FIELDS = new String[]{"tId", "lastModifiedBy", "lastModifiedTime"};

    @Column(name = "LAST_MODIFIED_BY", length = 100, nullable = true, columnDefinition = "VARCHAR(100)")
    private String lastModifiedBy;

    @Column(name = "LAST_MODIFIED_TS", nullable = false, columnDefinition = "TIMESTAMP")
    private Timestamp lastModifiedTime;

    public Timestamp getLastModifiedTime() {
        if (lastModifiedTime == null) {
            return new Timestamp(0);
        }
        Timestamp ts = new Timestamp(lastModifiedTime.getTime());
        ts.setNanos(lastModifiedTime.getNanos());
        return ts;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @PrePersist
    @PreUpdate
    void onUpdate() {
        lastModifiedBy = "default hard coded user";
        lastModifiedTime = new Timestamp(System.currentTimeMillis());
    }

    private BeanManager getBeanManager() {
        try {
            InitialContext ctx = new InitialContext();
            return (BeanManager) ctx.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            throw new RuntimeException("Unable to lookup CDI bean manager", e);
        }
    }

    /**
     * Override this method to set up the list of fields to be excluded from {@link #equals(Object)} and {@link #hashCode()}. The
     * default excludes are <code>tId, lastModifiedTime, lastModifiedBy</code>.
     */
    protected String[] excludedFields() {
        return DEFAULT_EXCLUDED_FIELDS;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, excludedFields());
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, excludedFields());
    }
}
