package io.openapitools.rest.common.persistence.vendor.hibernate;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.engine.jndi.JndiException;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform} which
 * are able to run both WebLogic and JBoss.
 *
 * @see org.hibernate.engine.transaction.jta.platform.internal.JBossAppServerJtaPlatform
 * @see org.hibernate.engine.transaction.jta.platform.internal.WeblogicJtaPlatform
 */
public class JtaPlatform extends AbstractJtaPlatform {
    private static final Logger LOG = LoggerFactory.getLogger(JtaPlatform.class);

    private Type type;

    @Override
    protected boolean canCacheUserTransactionByDefault() {
        return getType().canCache();
    }

    @Override
    protected boolean canCacheTransactionManagerByDefault() {
        return getType().canCache();
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return (TransactionManager) jndiService().locate(getType().getTmName());
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return (UserTransaction) jndiService().locate(getType().getUtName());
    }

    private Type getType() {
        if (type != null) {
            return type;
        }
        for (Type t : Type.values()) {
            try {
                jndiService().locate(t.getTmName());
                type = t;
                LOG.info("Transaction type detected as {}", t);
            } catch (JndiException e) {
                // Ignore
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction manager name could not be detected!");
        }
        return type;
    }

    enum Type {
        JBOSS("java:jboss/TransactionManager", "java:comp/UserTransaction", true),
        WEBLOGIC("javax.transaction.TransactionManager", "javax.transaction.UserTransaction", false);

        private final String tmName;
        private final String utName;
        private final boolean canCache;

        Type(String tmName, String utName, boolean canCache) {
            this.tmName = tmName;
            this.utName = utName;
            this.canCache = canCache;
        }

        public String getTmName() {
            return tmName;
        }

        public String getUtName() {
            return utName;
        }

        public boolean canCache() {
            return canCache;
        }
    }

}
