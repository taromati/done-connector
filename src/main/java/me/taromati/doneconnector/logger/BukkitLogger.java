package me.taromati.doneconnector.logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class BukkitLogger implements Logger {
    private static final String prefix = ChatColor.AQUA + "[TRMT] ";
    private final boolean enableDebug;

    public BukkitLogger(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

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
        if (this.enableDebug) {
            try {
                Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.LIGHT_PURPLE + msg);
            } catch (Exception ignored) {
                // Ignore exceptions
            }
        }
    }
}
