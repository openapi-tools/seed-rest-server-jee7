package io.openapitools.rest.common.core.logging;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for doing logging of durations hence the name ;-) .
 */
public final class DurationLogger implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DurationLogger.class);

    private static CurrentThreadLocal current = new CurrentThreadLocal();

    private long time;

    private long limit;

    private String clazzName;

    private String system;

    private String extra;

    private long duration;

    private List<DurationLogger> children = new ArrayList<>();

    private DurationLogger parent;

    /**
     * Construct a logging (starting a log time frame right away, this may be
     * restarted invoking {@link #start()}). Providing string class name
     */
    public DurationLogger(String clazzName, String system, String extra, long limit) {
        this.clazzName = clazzName;
        this.system = system;
        this.limit = limit;
        this.extra = extra;
        start();
    }

    /**
     * Constructor resolving the class name from the given object, and provided
     * with all possible variables.
     */
    public DurationLogger(Object obj, String system, String extra, long limit) {
        this(obj instanceof Class<?> ? ((Class<?>) obj).getName() : obj.getClass().getName(), system, extra, limit);
    }

    /**
     * Returns the duration measured in the last timeframe (ended by invoking
     * {@link #stop()}).
     *
     * @return The measure in milliseconds of last timeframe
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Marks the beginning of the current timeframe.
     *
     * @return current instance
     */
    public DurationLogger start() {
        time = System.nanoTime();
        children = new ArrayList<>();
        if (!equals(current.getCurrent())) {
            if (current.getCurrent() != null) {
                parent = current.getCurrent();
            }
            current.setCurrent(this);
        }
        return this;
    }

    /**
     * Marks the end of the current timeframe logging the time with log4j and to
     * the statistics instance if present. If limit is exceeded a warning
     * logging will be done!
     */
    public void stop() {
        duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time);
        if (parent != null) {
            parent.addChild(this);
        }
        current.setCurrent(parent);
        log();
    }

    private void log() {
        String id = clazzName + ":" + system;
        StringBuilder text = new StringBuilder()
                .append("Duration of system (").append(id).append(extra != null ? "[" + extra + "]" : "").append(")");
        StringBuilder child = new StringBuilder();
        if (!children.isEmpty()) {
            long childTime = 0;
            for (DurationLogger logger : children) {
                child.append(MessageFormat.format("\n({0}:{1}): {2} [ms]", logger.clazzName, logger.system, logger.getDuration()));
                childTime += logger.getDuration();
            }
            child.append("\nChild total: ").append(childTime).append(" [ms]");
        }
        if (duration > limit) {
            text.append(" exceeded limit (").append(limit).append(" [ms]) was ").append(duration).append(" [ms]").append(child);
            LOGGER.warn(text.toString());
        } else {
            LOGGER.debug(text + ": " + duration + " [ms]" + child);
        }
    }

    private void addChild(DurationLogger dl) {
        children.add(dl);
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    /**
     * ThreadLocal specialization to hold weak reference to LOGGER. Thus if no
     * instances other than this hold a references to a duration LOGGER instance
     * it will be garbage collected!
     */
    private static class CurrentThreadLocal extends ThreadLocal<WeakReference<DurationLogger>> {

        public void setCurrent(DurationLogger logger) {
            set(new WeakReference<>(logger));
        }

        public DurationLogger getCurrent() {
            WeakReference<DurationLogger> ref = get();
            if (ref != null) {
                return ref.get();
            } else {
                return null;
            }
        }
    }
}
