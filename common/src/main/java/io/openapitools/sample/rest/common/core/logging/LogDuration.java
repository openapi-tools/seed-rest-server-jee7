package io.openapitools.sample.rest.common.core.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Annotation to indicate that the duration of the annotated method should be
 * logged.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogDuration {
    /**
     * The name of the class doing the logging. Will default to the class of the
     * method.
     */
    @Nonbinding Class<? extends Object> clazzName() default Object.class;

    /**
     * The name of the operation within the class. Will default to the method
     * name.
     */
    @Nonbinding String system() default "";

    /**
     * Limit for the expected duration. Should the duration exceed this a
     * warning will be logged.
     */
    @Nonbinding long limit();
}
