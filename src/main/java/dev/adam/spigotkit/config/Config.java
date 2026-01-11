package dev.adam.spigotkit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified configuration wrapper for YAML and JSON formats.
 */
public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final JavaPlugin plugin;
    private final File file;
    private final Format format;

    private FileConfiguration yamlConfig;
    private JsonObject jsonRoot;

    private Config(JavaPlugin plugin, File file, Format format) {
        this.plugin = plugin;
        this.file = file;
        this.format = format;
    }

    public static Config load(JavaPlugin plugin, String baseName, Format format) {
        if (format == null) {
            format = Format.AUTO;
        }

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File targetFile;
        Format resolvedFormat;

        if (format == Format.AUTO) {
            File yml = new File(dataFolder, baseName + ".yml");
            File yaml = new File(dataFolder, baseName + ".yaml");
            File json = new File(dataFolder, baseName + ".json");
            if (yml.exists()) {
                targetFile = yml;
                resolvedFormat = Format.YAML;
            } else if (yaml.exists()) {
                targetFile = yaml;
                resolvedFormat = Format.YAML;
            } else if (json.exists()) {
                targetFile = json;
                resolvedFormat = Format.JSON;
            } else {
                targetFile = yml;
                resolvedFormat = Format.YAML;
            }
        } else if (format == Format.YAML) {
            targetFile = new File(dataFolder, baseName + ".yml");
            resolvedFormat = Format.YAML;
        } else {
            targetFile = new File(dataFolder, baseName + ".json");
            resolvedFormat = Format.JSON;
        }

        Config config = new Config(plugin, targetFile, resolvedFormat);
        config.ensureDefaults(baseName);
        config.reload();
        return config;
    }

    private void ensureDefaults(String baseName) {
        if (file.exists()) {
            return;
        }

        String resourceName;
        if (format == Format.JSON) {
            resourceName = baseName + ".json";
        } else {
            resourceName = baseName + ".yml";
        }

        InputStream in = plugin.getResource(resourceName);
        if (in == null) {
            return;
        }

        OutputStream out = null;
        try {
            file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save default config resource: " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void reload() {
        if (format == Format.YAML) {
            this.yamlConfig = YamlConfiguration.loadConfiguration(file);
        } else {
            Reader reader = null;
            try {
                if (!file.exists()) {
                    this.jsonRoot = new JsonObject();
                    return;
                }
                reader = new FileReader(file);
                JsonElement element = GSON.fromJson(reader, JsonElement.class);
                if (element == null || element.isJsonNull()) {
                    this.jsonRoot = new JsonObject();
                } else if (element.isJsonObject()) {
                    this.jsonRoot = element.getAsJsonObject();
                } else {
                    this.jsonRoot = new JsonObject();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load JSON config: " + e.getMessage());
                this.jsonRoot = new JsonObject();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public void save() {
        if (format == Format.YAML) {
            try {
                yamlConfig.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save YAML config: " + e.getMessage());
            }
        } else {
            Writer writer = null;
            try {
                file.getParentFile().mkdirs();
                writer = new FileWriter(file);
                GSON.toJson(jsonRoot != null ? jsonRoot : new JsonObject(), writer);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save JSON config: " + e.getMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public boolean contains(String path) {
        if (format == Format.YAML) {
            return yamlConfig.contains(path);
        }
        return getJson(path) != null && !getJson(path).isJsonNull();
    }

    public String getString(String path) {
        if (format == Format.YAML) {
            return yamlConfig.getString(path);
        }
        JsonElement el = getJson(path);
        return el != null && el.isJsonPrimitive() ? el.getAsString() : null;
    }

    public String getString(String path, String def) {
        String value = getString(path);
        return value != null ? value : def;
    }

    public int getInt(String path) {
        if (format == Format.YAML) {
            return yamlConfig.getInt(path);
        }
        JsonElement el = getJson(path);
        return el != null && el.isJsonPrimitive() ? el.getAsInt() : 0;
    }

    public int getInt(String path, int def) {
        if (!contains(path)) {
            return def;
        }
        return getInt(path);
    }

    public boolean getBoolean(String path) {
        if (format == Format.YAML) {
            return yamlConfig.getBoolean(path);
        }
        JsonElement el = getJson(path);
        return el != null && el.isJsonPrimitive() && el.getAsBoolean();
    }

    public boolean getBoolean(String path, boolean def) {
        if (!contains(path)) {
            return def;
        }
        return getBoolean(path);
    }

    public double getDouble(String path) {
        if (format == Format.YAML) {
            return yamlConfig.getDouble(path);
        }
        JsonElement el = getJson(path);
        return el != null && el.isJsonPrimitive() ? el.getAsDouble() : 0.0D;
    }

    public double getDouble(String path, double def) {
        if (!contains(path)) {
            return def;
        }
        return getDouble(path);
    }

    public List<String> getStringList(String path) {
        if (format == Format.YAML) {
            return yamlConfig.getStringList(path);
        }
        JsonElement el = getJson(path);
        List<String> list = new ArrayList<String>();
        if (el != null && el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            for (JsonElement item : arr) {
                if (item.isJsonPrimitive()) {
                    list.add(item.getAsString());
                }
            }
        }
        return list;
    }

    public List<String> getStringList(String path, List<String> def) {
        List<String> list = getStringList(path);
        return list != null && !list.isEmpty() ? list : def;
    }

    public List<Integer> getIntList(String path) {
        if (format == Format.YAML) {
            List<Integer> list = new ArrayList<Integer>();
            for (Object o : yamlConfig.getList(path)) {
                if (o instanceof Number) {
                    list.add(((Number) o).intValue());
                }
            }
            return list;
        }
        JsonElement el = getJson(path);
        List<Integer> list = new ArrayList<Integer>();
        if (el != null && el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            for (JsonElement item : arr) {
                if (item.isJsonPrimitive()) {
                    list.add(item.getAsInt());
                }
            }
        }
        return list;
    }

    public List<Integer> getIntList(String path, List<Integer> def) {
        List<Integer> list = getIntList(path);
        return list != null && !list.isEmpty() ? list : def;
    }

    public void set(String path, Object value) {
        if (format == Format.YAML) {
            yamlConfig.set(path, value);
            return;
        }
        if (jsonRoot == null) {
            jsonRoot = new JsonObject();
        }
        String[] parts = path.split("\\.");
        JsonObject current = jsonRoot;
        for (int i = 0; i < parts.length - 1; i++) {
            String key = parts[i];
            JsonElement child = current.get(key);
            if (child == null || !child.isJsonObject()) {
                JsonObject obj = new JsonObject();
                current.add(key, obj);
                current = obj;
            } else {
                current = child.getAsJsonObject();
            }
        }
        String lastKey = parts[parts.length - 1];
        current.add(lastKey, toJsonElement(value));
    }

    private JsonElement getJson(String path) {
        if (jsonRoot == null) {
            return JsonNull.INSTANCE;
        }
        String[] parts = path.split("\\.");
        JsonElement current = jsonRoot;
        for (int i = 0; i < parts.length; i++) {
            if (!current.isJsonObject()) {
                return JsonNull.INSTANCE;
            }
            JsonObject obj = current.getAsJsonObject();
            current = obj.get(parts[i]);
            if (current == null) {
                return JsonNull.INSTANCE;
            }
        }
        return current;
    }

    private JsonElement toJsonElement(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof Number) {
            return GSON.toJsonTree(value);
        }
        if (value instanceof Boolean) {
            return GSON.toJsonTree(value);
        }
        if (value instanceof String) {
            return GSON.toJsonTree(value);
        }
        if (value instanceof List) {
            return GSON.toJsonTree(value);
        }
        return GSON.toJsonTree(value);
    }
}
