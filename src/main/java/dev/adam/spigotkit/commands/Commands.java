package dev.adam.spigotkit.commands;

import java.util.function.Consumer;

/**
 * Small convenience helpers for registering commands.
 */
public final class Commands {

    private Commands() {
    }

    /**
     * Register a simple player-only command with an optional permission.
     */
    public static void playerCommand(CommandManager manager,
                                     String name,
                                     String permission,
                                     String description,
                                     CommandExecutorFn executor) {
        manager.register(name, cmd -> {
            if (permission != null && !permission.isEmpty()) {
                cmd.permission(permission);
            }
            cmd.playerOnly();
            if (description != null && !description.isEmpty()) {
                cmd.description(description);
            }
            cmd.exec(executor);
        });
    }

    /**
     * Register a simple command (not forced player-only).
     */
    public static void command(CommandManager manager,
                               String name,
                               String description,
                               CommandExecutorFn executor) {
        manager.register(name, cmd -> {
            if (description != null && !description.isEmpty()) {
                cmd.description(description);
            }
            cmd.exec(executor);
        });
    }

    /**
     * Fully custom registration passthrough, for symmetry.
     */
    public static void command(CommandManager manager,
                               String name,
                               Consumer<CommandSpec.Builder> customizer) {
        manager.register(name, customizer);
    }
}
