package io.openapitools.rest.common.persistence.vendor.hibernate;

import org.hibernate.dialect.DB2390Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate dialect resolver to provide the correct DB2 zOS dialect for DB2.
 */
public class DB2DialectResolver implements DialectResolver {
    private static final Logger LOG = LoggerFactory.getLogger(DB2DialectResolver.class);

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        if ("DB2".equals(info.getDatabaseName())) {
            LOG.debug("DB2 detected returning DB2390Dialect");
            return new DB2390Dialect();
        }
        return null;
    }
}
