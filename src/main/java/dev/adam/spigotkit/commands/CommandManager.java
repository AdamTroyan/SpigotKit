package dev.adam.spigotkit.commands;

import dev.adam.spigotkit.core.ColorUtil;
import dev.adam.spigotkit.core.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central entry point for registering and routing commands.
 *
 * <p>Commands must still be declared in {@code plugin.yml}; this manager
 * removes the need for separate executor classes and provides a simple
 * fluent API around {@link CommandSpec}.</p>
 */
public final class CommandManager {

    private final Plugin plugin;
    private final Map<String, CommandSpec> commands = new HashMap<String, CommandSpec>();

    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin plugin() {
        return plugin;
    }

    /**
     * Register a command specification.
     * <p>
     * The command name must match an entry in {@code plugin.yml}.
     * </p>
     */
    public void register(CommandSpec spec) {
        String name = spec.name().toLowerCase(Locale.ROOT);
        commands.put(name, spec);

        PluginCommand pluginCommand = plugin.getServer().getPluginCommand(name);
        if (pluginCommand == null) {
            plugin.getLogger().warning("No plugin.yml command found for '" + name + "'. Skipping registration.");
            return;
        }

        InternalExecutor executor = new InternalExecutor(spec);
        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(executor);
    }

    /**
     * Convenience overload that allows registering a command without
     * explicitly calling {@code build()} on the {@link CommandSpec.Builder}.
     */
    public void register(String name, java.util.function.Consumer<CommandSpec.Builder> specConsumer) {
        CommandSpec.Builder builder = CommandSpec.create(name);
        specConsumer.accept(builder);
        register(builder.build());
    }

    private final class InternalExecutor implements CommandExecutor, TabCompleter {

        private final CommandSpec rootSpec;

        private InternalExecutor(CommandSpec rootSpec) {
            this.rootSpec = rootSpec;
        }

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            try {
                dispatch(sender, label, args);
            } catch (Throwable t) {
                Messages.logCommandError(plugin, rootSpec.name(), t);
                sender.sendMessage(ColorUtil.color(Messages.error()));
            }
            return true;
        }

        private void dispatch(CommandSender sender, String label, String[] args) {
            CommandSpec targetSpec = rootSpec;
            String[] effectiveArgs = args;

            if (rootSpec.hasSubcommands() && args.length > 0) {
                String subName = args[0].toLowerCase(Locale.ROOT);
                CommandSpec sub = rootSpec.subcommands().get(subName);
                if (sub != null) {
                    targetSpec = sub;
                    if (args.length > 1) {
                        effectiveArgs = Arrays.copyOfRange(args, 1, args.length);
                    } else {
                        effectiveArgs = new String[0];
                    }
                } else {
                    sender.sendMessage(ColorUtil.color(Messages.unknownSubcommand(label)));
                    showHelp(sender, rootSpec, label);
                    return;
                }
            }

            if (!checkSenderAndPermission(sender, targetSpec)) {
                return;
            }

            CommandContext ctx = new CommandContext(plugin, sender, label, effectiveArgs, targetSpec);
            CommandExecutorFn exec = targetSpec.executor();
            if (exec == null) {
                showHelp(sender, targetSpec, label);
                return;
            }

            exec.execute(ctx);
        }

        private boolean checkSenderAndPermission(CommandSender sender, CommandSpec spec) {
            if (spec.playerOnly() && !(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.color(Messages.playerOnly()));
                return false;
            }
            String permission = spec.permission();
            if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                sender.sendMessage(ColorUtil.color(Messages.noPermission()));
                return false;
            }
            return true;
        }

        private void showHelp(CommandSender sender, CommandSpec spec, String label) {
            String usage = spec.usage();
            if (usage != null && !usage.isEmpty()) {
                sender.sendMessage(ColorUtil.color(usage));
                return;
            }
            if (spec.hasSubcommands()) {
                sender.sendMessage(ColorUtil.color(ChatColor.YELLOW + "Available subcommands:"));
                for (Map.Entry<String, CommandSpec> entry : spec.subcommands().entrySet()) {
                    CommandSpec sub = entry.getValue();
                    if (!checkSenderAndPermission(sender, sub)) {
                        continue;
                    }
                    StringBuilder line = new StringBuilder();
                    line.append(ChatColor.YELLOW).append("/").append(label).append(" ").append(sub.name());
                    if (sub.description() != null && !sub.description().isEmpty()) {
                        line.append(" ")
                            .append(ChatColor.GRAY).append("- ")
                            .append(ChatColor.WHITE).append(sub.description());
                    }
                    sender.sendMessage(ColorUtil.color(line.toString()));
                }
            } else {
                sender.sendMessage(ColorUtil.color(ChatColor.YELLOW + "Usage: /" + label));
            }
        }

        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            try {
                return tab(sender, alias, args);
            } catch (Throwable t) {
                Messages.logCommandError(plugin, rootSpec.name(), t);
                return Collections.emptyList();
            }
        }

        private List<String> tab(CommandSender sender, String label, String[] args) {
            if (args.length == 0) {
                return Collections.emptyList();
            }

            if (rootSpec.hasSubcommands()) {
                if (args.length == 1) {
                    String prefix = args[0].toLowerCase(Locale.ROOT);
                    List<String> suggestions = new ArrayList<String>();
                    for (Map.Entry<String, CommandSpec> entry : rootSpec.subcommands().entrySet()) {
                        CommandSpec sub = entry.getValue();
                        if (!checkSenderAndPermission(sender, sub)) {
                            continue;
                        }
                        String name = sub.name();
                        if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            suggestions.add(name);
                        }
                    }
                    return suggestions;
                }

                String subName = args[0].toLowerCase(Locale.ROOT);
                CommandSpec sub = rootSpec.subcommands().get(subName);
                if (sub != null) {
                    if (!checkSenderAndPermission(sender, sub)) {
                        return Collections.emptyList();
                    }
                    String[] subArgs;
                    if (args.length > 1) {
                        subArgs = Arrays.copyOfRange(args, 1, args.length);
                    } else {
                        subArgs = new String[0];
                    }
                    CommandContext ctx = new CommandContext(plugin, sender, label, subArgs, sub);
                    TabCompleterFn tabCompleter = sub.tabCompleter();
                    if (tabCompleter != null) {
                        List<String> result = tabCompleter.complete(ctx);
                        return result != null ? result : Collections.<String>emptyList();
                    }
                }
            }

            TabCompleterFn rootTab = rootSpec.tabCompleter();
            if (rootTab == null) {
                return Collections.emptyList();
            }
            CommandContext ctx = new CommandContext(plugin, sender, label, args, rootSpec);
            List<String> result = rootTab.complete(ctx);
            return result != null ? result : Collections.<String>emptyList();
        }
    }
}
