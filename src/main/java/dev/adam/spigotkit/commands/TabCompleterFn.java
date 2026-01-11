package dev.adam.spigotkit.commands;

import java.util.List;

/**
 * Functional interface for providing tab completion suggestions.
 */
public interface TabCompleterFn {

    /**
     * Compute tab completion suggestions for the given context.
     *
     * @param ctx command context
     * @return list of suggestions, never {@code null}
     */
    List<String> complete(CommandContext ctx);
}
