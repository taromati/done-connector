package me.taromati.doneconnector.logger;

import me.taromati.doneconnector.DoneConnector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Implementation of ILogger interface that uses Bukkit's console sender for logging.
 */
public class BukkitLogger implements Logger {
    private static final String prefix = ChatColor.AQUA + "[TRMT] ";

    @Override
    public void info(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.WHITE + msg);
        } catch (Exception ignored) {
            // Ignore exceptions
        }
    }

    @Override
    public void done(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + msg);
        } catch (Exception ignored) {
            // Ignore exceptions
        }
    }

    @Override
    public void error(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + msg);
        } catch (Exception ignored) {
            // Ignore exceptions
        }
    }

    @Override
    public void warn(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + msg);
        } catch (Exception ignored) {
            // Ignore exceptions
        }
    }

    @Override
    public void debug(String msg) {
        if (DoneConnector.debug) {
            try {
                Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.LIGHT_PURPLE + msg);
            } catch (Exception ignored) {
                // Ignore exceptions
            }
        }
    }

    @Override
    public void say(String msg) {
        String command = "say " + msg;

        Bukkit.getScheduler()
                .callSyncMethod(DoneConnector.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
