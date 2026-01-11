package dev.adam.spigotkit.core;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

/**
 * Central message templates used by SpigotKit. These can be overridden
 * at runtime by plugins if desired.
 */
public final class Messages {

    private static String noPermission = ChatColor.RED + "You do not have permission to do that.";
    private static String playerOnly = ChatColor.RED + "Only players may use this command.";
    private static String error = ChatColor.RED + "An internal error occurred while attempting to perform this command.";
    private static String unknownSubcommand = ChatColor.RED + "Unknown subcommand. Use /%s for help.";

    private Messages() {
    }

    public static String noPermission() {
        return noPermission;
    }

    public static String playerOnly() {
        return playerOnly;
    }

    public static String error() {
        return error;
    }

    public static String unknownSubcommand(String label) {
        return String.format(unknownSubcommand, label);
    }

    public static void setNoPermission(String message) {
        noPermission = message;
    }

    public static void setPlayerOnly(String message) {
        playerOnly = message;
    }

    public static void setError(String message) {
        error = message;
    }

    public static void setUnknownSubcommand(String messageTemplate) {
        unknownSubcommand = messageTemplate;
    }

    /**
     * Simple helper to log an exception related to command execution.
     */
    public static void logCommandError(Plugin plugin, String commandName, Throwable throwable) {
        plugin.getLogger().severe("Error while executing command '" + commandName + "': " + throwable.getMessage());
    }
}
