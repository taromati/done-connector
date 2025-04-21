package me.taromati.doneconnector.logger;

/**
 * Factory class for creating Logger instances.
 */
public class LoggerFactory {
    private static Logger defaultLogger = new SystemLogger(false);

    /**
     * Get the default logger.
     * @return The default logger
     */
    public static Logger getLogger() {
        return defaultLogger;
    }

    /**
     * Set the default logger.
     * @param logger The logger to set as default
     */
    public static void setLogger(Logger logger) {
        defaultLogger = logger;
    }

    /**
     * Create a new BukkitLogger.
     * @return A new BukkitLogger
     */
    public static Logger createBukkitLogger(boolean enableDebug) {
        return new BukkitLogger(enableDebug);
    }

    /**
     * Create a new SystemLogger.
     * @return A new SystemLogger
     */
    public static Logger createSystemLogger(boolean enableDebug) {
        return new SystemLogger(enableDebug);
    }
}
