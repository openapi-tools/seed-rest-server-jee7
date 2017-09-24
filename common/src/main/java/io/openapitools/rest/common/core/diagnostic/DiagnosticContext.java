package io.openapitools.rest.common.core.diagnostic;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Handling diagnostic information in the context tagging all log entries with a specific
 * identifier - log token.
 */
public class DiagnosticContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticContext.class);

    private static final String MDC_KEY = "logToken";

    private final ContextInfo contextInfo;

    public DiagnosticContext(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    /**
     * Invoked to signal the start of a processing context.
     */
    public void start() {
        start(null);
    }

    /**
     * Invokes to signal the start of a processing context. If the current {@link ContextInfo} does
     * not contain a token a new is created logging the given user info attributes.
     *
     * @param userInfo User information to log when extracting new token
     */
    public void start(Map<String, String> userInfo) {
        if (contextInfo.getLogToken() == null) {
            String logToken = UUID.randomUUID().toString();
            contextInfo.setLogToken(logToken);
            MDC.put(MDC_KEY, logToken);
            log(userInfo);
        } else {
            MDC.put(MDC_KEY, contextInfo.getLogToken());
        }
    }

    /**
     * Invoked to signal the end of a processing context.
     */
    public void stop() {
        MDC.remove(MDC_KEY);
    }

    protected void log(Map<String, String> userInfo) {
        appendUserInfo("[ New request by following identifiers: ", userInfo);
    }

    /**
     * Get the token of the current processing context.
     *
     * @return the active token
     */
    public static String getLogToken() {
        return MDC.get(MDC_KEY);
    }

    private static void appendUserInfo(String prefix, Map<String, String> userInfo) {
        StringBuilder logMsg = new StringBuilder(prefix);
        if (userInfo != null) {
            for (Map.Entry<String, String> entry : userInfo.entrySet()) {
                logMsg.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
            }
        } else {
            logMsg.append("N/A ");
        }
        logMsg.append("with the logtoken=").append(getLogToken()).append(" ]");
        LOGGER.info(logMsg.toString());
    }

}
