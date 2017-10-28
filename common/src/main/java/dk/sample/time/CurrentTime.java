package dk.sample.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A very simple way of controlling current time.
 *
 * can be used in tests for avoiding time related challenges and thus remove false positives in test.
 * to be used remotely the remote instance must expose an endpoint which allows the support of this feature.
 */

public class CurrentTime {

    private static ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
    private static boolean stopped;

    protected CurrentTime() {
        // no construction necessary except from derived usages
    }

    /**
     * @return current time to in UTC time, the time can be real or set as "virtual" current time
     */
    public static ZonedDateTime nowAsZonedDateTime() {
        if (stopped) {
            return time;
        }
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * @return current time to in UTC time, the time can be real or set as "virtual" current time
     */
    public static Instant now() {
        if (stopped) {
            return time.toInstant();
        }
        return Instant.now();
    }

    protected static void setTime(Instant instant) {
        time = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        stopped = false;
    }

    protected static void setTime(Instant instant, boolean stopTime) {
        time = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        stopped = stopTime;
    }

    protected static void startTime() {
        stopped = false;
    }

    protected static void stopTime() {
        stopped = true;
    }

    protected static void reset() {
        time = ZonedDateTime.now(ZoneId.of("UTC"));
        stopped = false;
    }

}
