package me.taromati.doneconnector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
    private static final String prefix = ChatColor.AQUA + "[TRMT] ";

    public static void info(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.WHITE + msg);
        } catch (Exception ignored) {

        }
    }

    public static void error(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + msg);
        } catch (Exception ignored) {

        }
    }

    public static void warn(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + msg);
        } catch (Exception ignored) {

        }
    }

    public static void debug(String msg) {
        if (DoneConnector.debug) {
            try {
                Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.LIGHT_PURPLE + msg);
            } catch (Exception ignored) {

            }
        }
    }

    public static void say(String msg) {
        String command = "say " + msg;

        Bukkit.getScheduler()
                .callSyncMethod(DoneConnector.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
