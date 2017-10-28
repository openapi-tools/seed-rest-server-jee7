package io.openapitools.sample.rest.common.core.logging;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * CDI interceptor that will look for {@link LogDuration} annotations on the intercepted methods.
 */
@Interceptor
@LogDuration(limit = 0)
@Priority(Interceptor.Priority.APPLICATION)
public class LogDurationInterceptor {

    private static final int MAX_PARAM_VALUE_LENGTH = 100;

    @AroundInvoke
    public Object logDuration(InvocationContext ctx) throws Exception {
        if (ctx.getMethod().isAnnotationPresent(LogDuration.class)) {
            LogDuration logDuration = ctx.getMethod().getAnnotation(LogDuration.class);
            Class<?> clazzName = Object.class.equals(logDuration.clazzName()) ? ctx.getTarget().getClass() : logDuration.clazzName();
            String system = "".equals(logDuration.system()) ? ctx.getMethod().getName() : logDuration.system();
            try (DurationLogger ignored = new DurationLogger(clazzName, system, paramsToString(ctx.getParameters()), logDuration.limit())) {
                return ctx.proceed();
            }
        } else {
            return ctx.proceed();
        }
    }

    private String paramsToString(Object[] parameters) {
        if (parameters != null && parameters.length > 0) {
            return Arrays.stream(parameters).map(this::paramToString).collect(Collectors.joining(","));
        } else {
            return null;
        }
    }

    private String paramToString(Object parameter) {
        String textValue = String.valueOf(parameter);
        int length = textValue.length();
        if (length > MAX_PARAM_VALUE_LENGTH) {
            String truncatedValue = textValue.substring(0, MAX_PARAM_VALUE_LENGTH).replace('\n', ' ').replace("\r", "");
            textValue = String.format("%s... (was %d chars long, truncated)", truncatedValue, length);
        }
        return textValue;
    }
}
