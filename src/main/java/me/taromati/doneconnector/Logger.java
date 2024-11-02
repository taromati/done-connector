package me.taromati.doneconnector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
    private static final String prefix = ChatColor.AQUA + "[TRMT] ";

    public static void info(String msg) {
        String fullMsg = prefix + msg;

        try {
            Bukkit.getConsoleSender().sendMessage(fullMsg);
        } catch (Exception e) {
            if (!DoneConnector.debug) {
                System.out.println("Error: " + e);
            }
        }

        if (DoneConnector.debug) {
            System.out.println(fullMsg);
        }
    }

    public static void error(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + msg);
        } catch (Exception e) {
            if (!DoneConnector.debug) {
                System.out.println("Error: " + e);
            }
        }

        if (DoneConnector.debug) {
            System.err.println(prefix + msg);
        }
    }

    public static void warn(String msg) {
        try {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + msg);
        } catch (Exception e) {
            if (!DoneConnector.debug) {
                System.out.println("Error: " + e);
            }
        }

        if (DoneConnector.debug) {
            System.out.println(prefix + msg);
        }
    }

    public static void debug(String msg) {
        if (DoneConnector.debug) {
            try {
                Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.LIGHT_PURPLE + msg);
            } catch (Exception e) {
                if (!DoneConnector.debug) {
                    System.out.println("Error: " + e);
                }
            }

            System.out.println(prefix + ChatColor.LIGHT_PURPLE + msg);
        }
    }

    public static void say(String msg) {
        String command = "say " + msg;

        Bukkit.getScheduler()
                .callSyncMethod(DoneConnector.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

        if (DoneConnector.debug) {
            System.out.println(command);
        }
    }
}
