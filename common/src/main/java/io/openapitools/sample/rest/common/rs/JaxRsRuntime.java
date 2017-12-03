package io.openapitools.sample.rest.common.rs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.RuntimeDelegate;

import io.openapitools.sample.rest.common.rs.filter.OriginFilter;
import io.openapitools.sample.rest.common.rs.jersey.DisableJerseyMOXyFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for detecting JAX-RS runtime. The class can be used for
 * correctly configuring exception mapping, providers and features in a JAX-RS application. Thus in the
 * {@link javax.ws.rs.core.Application} implementation class override the
 * <code>getClasses()</code> method and add the results from
 * <code>JaxRsRuntime.getExceptionMappers()</code>, <code>JaxRsRuntime.getSerializers()</code> and/or <code>JaxRsRuntime.getFilters()</code>
 * - e.g.:
 * <br><br>
 * <pre>{@code
 * public Set<Class<?>> getClasses() {
 *    Set<Class<?>> classes = new HashSet<>();
 *    classes.addAll(Arrays.asList(YourServiceExposure.class));
 *    classes.addAll(JaxRsRuntime.getExceptionMappers());
 *    classes.addAll(JaxRsRuntime.getSerializers());
 *    classes.addAll(JaxRsRuntime.getFilters());
 *    return classes;
 * }
 * }</pre>
 * or use <code>JaxRsRuntime.configure(classes)</code> for complete configuration:
 * <br><br>
 * <pre>{@code
 * public Set<Class<?>> getClasses() {
 *    Set<Class<?>> classes = new HashSet<>();
 *    classes.addAll(Arrays.asList(YourServiceExposure.class));
 *    JaxRsRuntime.configure(classes);
 *    return classes;
 * }
 * }</pre>
 */
public final class JaxRsRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsRuntime.class);

    private static Runtime runtime;

    private JaxRsRuntime() {
    }

    public static Runtime runtime() {
        if (runtime == null) {
            runtime = Runtime.get(RuntimeDelegate.getInstance().getClass().getName());
        }
        return runtime;
    }

    /**
     * Enum of supported runtimes.
     */
    public enum Runtime {

        JERSEY("Jersey", "jersey"),
        RESTEASY("Rest Easy", "resteasy"),
        UNKNOWN("Unknown", null);

        private final String name;
        private final String runtimeDelegatePattern;

        Runtime(String name, String runtimeDelegatePattern) {
            this.name = name;
            this.runtimeDelegatePattern = runtimeDelegatePattern;
        }

        public String getName() {
            return name;
        }

        private static Runtime get(String runtimePackage) {
            for (Runtime rt : Runtime.values()) {
                if (runtimePackage.contains(rt.runtimeDelegatePattern)) {
                    LOG.info("{} JAX-RS runtime detected.", rt.getName());
                    return rt;
                }
            }
            LOG.warn("Unsupported JAX-RS runtime detected. Using default runtime validation exception mapper and features");
            return UNKNOWN;
        }
    }

    public static Set<Class<?>> getExceptionMappers() {
        return Collections.emptySet();
    }

    public static Set<Class<?>> getSerializers() {
        Set<Class<?>> serializers = new HashSet<>(Collections.singleton(HalJsonProvider.class));
        if (runtime() == Runtime.JERSEY) {
            serializers.add(DisableJerseyMOXyFeature.class);
        }
        return serializers;
    }

    public static Set<Class<?>> getFilters() {
        return new HashSet<>(Collections.singletonList(OriginFilter.class));
    }

    public static void configure(Set<Class<?>> classes) {
        classes.addAll(getExceptionMappers());
        classes.addAll(getSerializers());
        classes.addAll(getFilters());
    }
}
