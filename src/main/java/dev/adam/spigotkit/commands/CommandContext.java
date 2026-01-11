package dev.adam.spigotkit.commands;

import dev.adam.spigotkit.core.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Context passed to command executors and tab completers.
 */
public final class CommandContext {

    private final Plugin plugin;
    private final CommandSender sender;
    private final String label;
    private final String[] args;
    private final CommandSpec commandSpec;

    public CommandContext(Plugin plugin, CommandSender sender, String label, String[] args, CommandSpec commandSpec) {
        this.plugin = plugin;
        this.sender = sender;
        this.label = label;
        this.args = args;
        this.commandSpec = commandSpec;
    }

    public Plugin plugin() {
        return plugin;
    }

    public CommandSender sender() {
        return sender;
    }

    public Player player() {
        return sender instanceof Player ? (Player) sender : null;
    }

    /**
     * Returns the sender as a {@link Player} or throws an
     * {@link IllegalStateException} after sending a standard
     * "player only" message when the sender is not a player.
     */
    public Player requirePlayer() {
        Player player = player();
        if (player == null) {
            reply("Only players may use this command.");
            throw new IllegalStateException("Command requires player sender");
        }
        return player;
    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public int argsLen() {
        return args.length;
    }

    public String arg(int index) {
        if (index < 0 || index >= args.length) {
            return null;
        }
        return args[index];
    }

    public String joinArgs(int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (fromIndex >= args.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = fromIndex; i < args.length; i++) {
            if (i > fromIndex) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    public CommandSpec spec() {
        return commandSpec;
    }

    /**
     * Sends a colored message to the sender.
     */
    public void reply(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(ColorUtil.color(message));
    }
}
