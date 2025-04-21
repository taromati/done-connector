package me.taromati.doneconnector.logger;

public class SystemLogger implements Logger {
    private static final String PREFIX = "[TRMT] ";
    private static final String INFO_PREFIX = "[INFO] ";
    private static final String DONE_PREFIX = "[DONE] ";
    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String WARN_PREFIX = "[WARN] ";
    private static final String DEBUG_PREFIX = "[DEBUG] ";

    private final boolean enableDebug;

    public SystemLogger(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    @Override
    public void info(String msg) {
        System.out.println(PREFIX + INFO_PREFIX + msg);
    }

    @Override
    public void done(String msg) {
        System.out.println(PREFIX + DONE_PREFIX + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println(PREFIX + ERROR_PREFIX + msg);
    }

    @Override
    public void warn(String msg) {
        System.out.println(PREFIX + WARN_PREFIX + msg);
    }

    @Override
    public void debug(String msg) {
        if (this.enableDebug) {
            System.out.println(PREFIX + DEBUG_PREFIX + msg);
        }
    }
}
