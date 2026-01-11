## SpigotKit

Tiny helper library for Spigot/Paper plugins.

The goal: **write less boring boilerplate** and more actual plugin logic.

What it gives you:

- Commands without a mess of separate executor classes
- Subcommands + tab completion that actually feel nice to use
- Simple config API that works the same for YAML *and* JSON
- A couple of small utilities (colors, messages)

Target: Java 8+ and Spigot/Paper 1.21+.

---

## What you get

### Commands

You still declare commands in `plugin.yml`, but from there everything is code-first and fluent:

- Register commands in code (no extra executor classes)
- Subcommands like `/home set`, `/home tp`, etc.
- Per-command options:
  - Permission
  - Player-only flag
  - Description / usage
  - Optional tab-completion callback
- Lambda-friendly: executors are just `void execute(CommandContext ctx)`.

### Config

One small wrapper that hides "is this YAML or JSON?" for you:

- Under the hood:
  - Bukkit `YamlConfiguration` for `.yml/.yaml`
  - Gson for `.json`
- `Config.load(plugin, "config", Format.AUTO)` will:
  - Use `config.yml` / `config.yaml` / `config.json` if they exist
  - Otherwise create a new YAML config by default
- Dotted paths everywhere: `demo.prefix`, `homes.max`, etc.
- Typed getters:
  - `getString`, `getInt`, `getBoolean`, `getDouble`
  - `getStringList`, `getIntList`
- Writes & helpers: `set(path, value)`, `contains(path)`, `save()`, `reload()`.

### Core utilities

- Color helper for legacy `&` color codes
- Central messages (no-permission, player-only, error) that you can override if you want.

---

## Installation (quick and dirty)

SpigotKit is a normal Maven project. Build it once locally, then depend on it from your plugins.

### 1. Install to your local Maven repository

From the SpigotKit project folder:

```bash
mvn clean install
```

### 2. Add dependency to your plugin

In your plugin's `pom.xml`:

```xml
<dependencies>
    <!-- your existing spigot-api dependency here -->

    <dependency>
        <groupId>dev.adam</groupId>
        <artifactId>SpigotKit</artifactId>
        <version>1.0</version>
    </dependency>
</dependencies>
```

If you shade your plugin (recommended), make sure the shade plugin pulls SpigotKit into the final JAR.

---

## Usage

### Basic command

Register a simple `/heal` command in your `JavaPlugin`:

```java
import dev.adam.spigotkit.commands.CommandManager;
import dev.adam.spigotkit.commands.CommandSpec;
import dev.adam.spigotkit.config.Config;
import dev.adam.spigotkit.config.Format;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    private CommandManager commands;
    private Config config;

    @Override
    public void onEnable() {
        this.commands = new CommandManager(this);
        this.config = Config.load(this, "config", Format.AUTO);

        registerCommands();
    }

    private void registerCommands() {
        commands.register("heal", cmd -> cmd
                .alias("h")
                .permission("kit.heal")
                .playerOnly()
                .description("Heals the player to full health")
                .exec(ctx -> {
                    Player player = ctx.requirePlayer();
                    player.setHealth(player.getMaxHealth());
                    ctx.reply(ChatColor.GREEN + "Healed!");
                })
        );
    }
}
```

Declare the command in your plugin's `plugin.yml`:

```yaml
commands:
  heal:
    description: Heals the executing player
    usage: /heal
    aliases: ["h"]
```

### Subcommands + tab completion

```java
commands.register("home", cmd -> cmd
        .permission("kit.home")
        .description("Home management")
        .sub("set", sub -> sub
                .playerOnly()
                .description("Set your home")
                .exec(ctx -> ctx.reply(ChatColor.GREEN + "Home set (demo only).")))
        .sub("tp", sub -> sub
                .playerOnly()
                .description("Teleport to your home")
                .tab(ctx -> java.util.Collections.singletonList("base"))
                .exec(ctx -> ctx.reply(ChatColor.GREEN + "Teleporting to home (demo only).")))
);
```

If a root command has subcommands, the first arg is treated as the subcommand name. Tab completion on the first arg suggests subcommand names the sender is actually allowed to use.

### CommandContext helpers

`CommandContext` gives you:

- `plugin()` – the owning plugin
- `sender()` – `CommandSender`
- `player()` – `Player` or `null`
- `requirePlayer()` – returns `Player` or sends a standard "player only" message and throws
- `label()` – the command label used
- `args()` / `argsLen()` / `arg(int)` / `joinArgs(int fromIndex)`
- `spec()` – the effective `CommandSpec`
- `reply(String msg)` – sends a colored message (supports `&` codes).

---

## Config examples

### YAML (config.yml)

```yaml
chat:
  prefix: "&7[Server] &r"

feature:
  enabled: true

homes:
  max: 3
```

Usage:

```java
Config config = Config.load(this, "config", Format.AUTO);

String prefix = config.getString("chat.prefix");
boolean enabled = config.getBoolean("feature.enabled");
int maxHomes = config.getInt("homes.max");

config.set("homes.max", 5);
config.save();
```

### JSON (config.json)

```json
{
  "chat": {
    "prefix": "[Server] "
  },
  "feature": {
    "enabled": true
  }
}
```

Usage is identical, only the file format changes.

---

## Notes

- Target: Java 8+, Spigot/Paper 1.21+ (API-compatible with Paper).
- You still need to declare commands in `plugin.yml`; SpigotKit just wires the executors and tab completers for you.
- Command exceptions are caught and logged; players get a clean error message instead of a giant stack trace.

If you end up using this in one of your plugins – awesome. If not, fork it, rip out the bits you like, and make it your own. :) 
