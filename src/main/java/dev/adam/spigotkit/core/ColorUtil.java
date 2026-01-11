package dev.adam.spigotkit.core;

import org.bukkit.ChatColor;

/**
 * Utility methods for translating color codes in messages.
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    /**
     * Translates '&' color codes to Bukkit color codes.
     *
     * @param message input message, may be null
     * @return colored message, or null if input was null
     */
    public static String color(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
