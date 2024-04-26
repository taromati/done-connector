package me.taromati.doneconnector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
    private static final String prefix = ChatColor.AQUA + "[TRMT] ";

    public static void info(String msg) {
        Bukkit.getConsoleSender().sendMessage(prefix + msg);
    }
}
