package dev.adam.spigotkit.commands;

/**
 * Functional interface for executing a command.
 *
 * <p>Implementations should send any messages themselves and are not
 * required to return a status. Exceptions are caught and logged by
 * the framework.</p>
 */
public interface CommandExecutorFn {

    /**
     * Execute the command.
     *
     * @param ctx context for this execution
     */
    void execute(CommandContext ctx);
}
