package dev.adam.spigotkit.commands;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Immutable specification for a command or subcommand.
 */
public final class CommandSpec {

    private final String name;
    private final Set<String> aliases;
    private final String permission;
    private final boolean playerOnly;
    private final String description;
    private final String usage;
    private final Map<String, CommandSpec> subcommands;
    private final CommandExecutorFn executor;
    private final TabCompleterFn tabCompleter;

    private CommandSpec(Builder builder) {
        this.name = builder.name;
        this.aliases = Collections.unmodifiableSet(new LinkedHashSet<String>(builder.aliases));
        this.permission = builder.permission;
        this.playerOnly = builder.playerOnly;
        this.description = builder.description;
        this.usage = builder.usage;
        this.subcommands = Collections.unmodifiableMap(new LinkedHashMap<String, CommandSpec>(builder.subcommands));
        this.executor = builder.executor;
        this.tabCompleter = builder.tabCompleter;
    }

    public static Builder create(String name) {
        return new Builder(name);
    }

    public String name() {
        return name;
    }

    public Set<String> aliases() {
        return aliases;
    }

    public String permission() {
        return permission;
    }

    public boolean playerOnly() {
        return playerOnly;
    }

    public String description() {
        return description;
    }

    public String usage() {
        return usage;
    }

    public Map<String, CommandSpec> subcommands() {
        return subcommands;
    }

    public boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }

    public CommandExecutorFn executor() {
        return executor;
    }

    public TabCompleterFn tabCompleter() {
        return tabCompleter;
    }

    /**
     * Builder for {@link CommandSpec}.
     */
    public static final class Builder {

        private final String name;
        private final Set<String> aliases = new LinkedHashSet<String>();
        private String permission;
        private boolean playerOnly;
        private String description;
        private String usage;
        private final Map<String, CommandSpec> subcommands = new ConcurrentHashMap<String, CommandSpec>();
        private CommandExecutorFn executor;
        private TabCompleterFn tabCompleter;

        private Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Command name must not be empty");
            }
            this.name = name;
        }

        public Builder alias(String alias) {
            if (alias != null && !alias.isEmpty()) {
                this.aliases.add(alias.toLowerCase());
            }
            return this;
        }

        public Builder aliases(String... aliases) {
            if (aliases != null) {
                for (String alias : aliases) {
                    alias(alias);
                }
            }
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder playerOnly() {
            this.playerOnly = true;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder exec(CommandExecutorFn executor) {
            this.executor = executor;
            return this;
        }

        public Builder tab(TabCompleterFn tabCompleter) {
            this.tabCompleter = tabCompleter;
            return this;
        }

        /**
            * Define a subcommand by name.
            *
            * Usage:
            * <pre>
            * CommandSpec.create("home")
            *   .sub("set", sub -> sub.playerOnly().exec(...));
            * </pre>
            */
        public Builder sub(String name, Consumer<Builder> consumer) {
            Builder subBuilder = new Builder(name);
            consumer.accept(subBuilder);
            CommandSpec built = subBuilder.build();
            this.subcommands.put(name.toLowerCase(), built);
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(this);
        }
    }
}
