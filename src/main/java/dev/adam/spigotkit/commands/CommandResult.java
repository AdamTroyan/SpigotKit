package dev.adam.spigotkit.commands;

/**
 * Result of a command execution.
 */
public enum CommandResult {
    /** Command executed successfully. */
    SUCCESS,
    /** Command failed but handled its own messaging. */
    FAIL,
    /**
     * Command requests the framework to show usage/help.
     */
    SHOW_USAGE
}
