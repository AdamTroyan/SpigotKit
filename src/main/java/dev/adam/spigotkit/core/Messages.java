package dev.adam.spigotkit.core;

import dev.adam.spigotkit.config.Config;
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
     * Load message templates from a config file.
     * <p>
     * Expected keys (under the given base path):
     * <ul>
     *   <li>no-permission</li>
     *   <li>player-only</li>
     *   <li>error</li>
     *   <li>unknown-subcommand</li>
     * </ul>
     */
    public static void loadFromConfig(Config config, String basePath) {
        if (config == null) {
            return;
        }
        String prefix = basePath == null || basePath.isEmpty() ? "" : basePath + ".";
        noPermission = config.getString(prefix + "no-permission", noPermission);
        playerOnly = config.getString(prefix + "player-only", playerOnly);
        error = config.getString(prefix + "error", error);
        unknownSubcommand = config.getString(prefix + "unknown-subcommand", unknownSubcommand);
    }

    /**
     * Load message templates from a config file using the default
     * base path of {@code messages}.
     */
    public static void loadFromConfig(Config config) {
        loadFromConfig(config, "messages");
    }

    /**
     * Simple helper to log an exception related to command execution.
     */
    public static void logCommandError(Plugin plugin, String commandName, Throwable throwable) {
        plugin.getLogger().severe("Error while executing command '" + commandName + "': " + throwable.getMessage());
    }
}
