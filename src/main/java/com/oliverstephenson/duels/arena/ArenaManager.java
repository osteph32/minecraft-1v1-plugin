package com.oliverstephenson.duels.arena;

import com.oliverstephenson.duels.DuelsPlugin;
import com.oliverstephenson.duels.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ArenaManager {

    private final DuelsPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(DuelsPlugin plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    private void loadArenas() {
        arenas.clear();
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("arenas");
        if (section == null) {
            plugin.getLogger().warning("No 'arenas' section found in config.yml!");
            return;
        }

        for (String name : section.getKeys(false)) {
            ConfigurationSection arena = section.getConfigurationSection(name);
            if (arena == null) continue;

            Location spawn1 = LocationUtil.fromConfig(
                    arena.getConfigurationSection("spawn1"));
            Location spawn2 = LocationUtil.fromConfig(
                    arena.getConfigurationSection("spawn2"));

            if (spawn1 == null || spawn2 == null) {
                plugin.getLogger().warning("Arena '" + name
                        + "' has missing spawn points — skipping.");
                continue;
            }
            arenas.put(name.toLowerCase(), new Arena(name, spawn1, spawn2));
        }
    }

    public void reload() {
        plugin.reloadConfig();
        loadArenas();
    }

    public Optional<Arena> allocate() {
        List<Arena> available = arenas.values().stream()
                .filter(a -> !a.isInUse())
                .toList();
        if (available.isEmpty()) return Optional.empty();
        Arena chosen = available.get(new Random().nextInt(available.size()));
        chosen.setInUse(true);
        return Optional.of(chosen);
    }

    public void release(Arena arena) {
        arena.setInUse(false);
    }

    public void setSpawn(String arenaName, int spawnIndex, Location loc) {
        String path = "arenas." + arenaName + ".spawn" + spawnIndex;
        LocationUtil.toConfig(plugin.getConfig(), path, loc);
        plugin.saveConfig();
        reload();
    }

    public void setLobby(Location loc) {
        LocationUtil.toConfig(plugin.getConfig(), "lobby", loc);
        plugin.saveConfig();
    }

    public int              getArenaCount() { return arenas.size();   }
    public Collection<Arena> getArenas()    { return arenas.values(); }
}