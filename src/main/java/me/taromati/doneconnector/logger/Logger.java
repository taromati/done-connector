package me.taromati.doneconnector.logger;

/**
 * Logger interface for the DoneConnector plugin.
 * This interface defines methods for logging messages at different levels.
 */
public interface Logger {
    /**
     * Log an informational message.
     * @param msg The message to log
     */
    void info(String msg);

    /**
     * Log a message indicating that a task is done.
     * @param msg The message to log
     */
    void done(String msg);

    /**
     * Log an error message.
     * @param msg The message to log
     */
    void error(String msg);

    /**
     * Log a warning message.
     * @param msg The message to log
     */
    void warn(String msg);

    /**
     * Log a debug message.
     * @param msg The message to log
     */
    void debug(String msg);

    /**
     * Broadcast a message to all players.
     * @param msg The message to broadcast
     */
    void say(String msg);
}